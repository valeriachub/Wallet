package app.wallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by valeria on 20.04.18.
 */

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.text_wallet_address)
    TextView addressView;
    @BindView(R.id.text_eth_amound)
    TextView ethAmountView;
    @BindView(R.id.text_vcoin_amound)
    TextView vcoinAmountView;
    @BindView(R.id.edit_address)
    EditText addressToView;

    private Unbinder unbinder;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    private VCoin contract;
    private Credentials credentials;
    private Web3j web3j;

    private String fileName;
    private String password;
    private String address;

    public static void start(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_home);
        unbinder = ButterKnife.bind(this);
        getPreferences();
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void getPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences == null || !preferences.contains(Const.PREF_WALLET_ADDRESS) || preferences.getString(Const.PREF_WALLET_ADDRESS, "").isEmpty())
            return;

        fileName = preferences.getString(Const.PREF_WALLET_FILE_NAME, "");
        password = preferences.getString(Const.PREF_WALLET_PASSWORD, "");
        address = preferences.getString(Const.PREF_WALLET_ADDRESS, "");

        Log.e("app", "Address: " + address);
    }

    private void initUI() {
        if (!address.isEmpty()) {
            addressView.setText(address);

            getContract();
            getEthAmount();
            getVCoinAmount();
        }
    }

    private void getContract() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        try {
            credentials = WalletUtils.loadCredentials(password, path + "/" + fileName);
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }

        web3j = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/IcfLiiNmda5oaEpkKWJk"));

        try {
            Log.e("app", "Connected to Ethereum client version: "
                    + web3j.web3ClientVersion().sendAsync().get().getWeb3ClientVersion());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            contract = VCoin.load("0xefa2aa85cb164571f05041d1667a41ae94860fd7", web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
            Log.e("app", contract.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEthAmount() {
        if (web3j == null) return;

        try {
            BigInteger wei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get().getBalance();
            double eth = new BigDecimal(wei).divide(new BigDecimal(Math.pow(10, 18))).doubleValue();
            ethAmountView.setText(String.valueOf(eth) + " ETH");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getVCoinAmount() {
        if (contract == null) return;

        try {
            BigInteger vcoinAmount = contract.balanceOf(address).sendAsync().get();
            vcoinAmountView.setText(String.valueOf(vcoinAmount) + " VCOIN");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_send_token)
    void onTokenSendClicked() {
        if (contract == null) return;
//        try {
//            Log.e("app", "Start transaction");
//
//            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
//                    "0xa447d74b06eef44770ab95b6d4f7df60b7a6da21", DefaultBlockParameterName.LATEST).sendAsync().get();
//            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//            BigInteger gasPrice = Contract.GAS_PRICE;
//
//            BigInteger gasLimit = Contract.GAS_LIMIT;
//            final Function function = new Function(
//                    "transferFrom",
//                    Arrays.<Type>asList(new org.web3j.abi.datatypes.Address("0xa447d74b06eef44770ab95b6d4f7df60b7a6da21"),
//                            new org.web3j.abi.datatypes.Address("0x84e7F3bFc78f2Fc5d4Fe47AfdFC94A8cd3CD1CFd"),
//                            new org.web3j.abi.datatypes.generated.Uint256(1)),
//                    Collections.<TypeReference<?>>emptyList());
//
//            String functionEncoder = FunctionEncoder.encode(function);
//
//            Transaction transaction = Transaction.createFunctionCallTransaction(
//                    "0xa447d74b06eef44770ab95b6d4f7df60b7a6da21", nonce, gasPrice,
//                    gasLimit, "0xefa2aa85cb164571f05041d1667a41ae94860fd7", new BigInteger("0"),
//                    functionEncoder);
//            EthSendTransaction transactionResponse =
//                    web3j.ethSendTransaction(transaction).sendAsync().get();
//
//            Log.e("app", transactionResponse.toString());
//
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }

        String address = addressToView.getText().toString();
        if (!address.isEmpty()) {
            transfer(address);
        } else {
            Toast.makeText(this, "Please write an address", Toast.LENGTH_SHORT).show();
        }
    }

    private void transfer(String addressTo) {
        contract.transferFrom(address,
                addressTo, BigInteger.valueOf(5))
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.show();
                })
                .subscribe(transactionReceipt -> {
                    progressDialog.dismiss();
                    addressToView.setText("");
                    getEthAmount();
                    getVCoinAmount();
                }, throwable -> {
                    progressDialog.dismiss();
                    addressToView.setText("");
                    Log.e("app", throwable.toString());
                });
    }
}


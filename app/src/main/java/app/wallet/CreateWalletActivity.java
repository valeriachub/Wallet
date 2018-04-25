package app.wallet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by valeria on 20.04.18.
 */

public class CreateWalletActivity extends AppCompatActivity {

    @BindView(R.id.edit_password)
    EditText passwordView;

    private Unbinder unbinder;

    public static void start(Context context) {
        Intent intent = new Intent(context, CreateWalletActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_create_wallet);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_create)
    void onCreateClicked() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Const.REQUEST_PERMISSION_WRITE_STORAGE);
        } else {
            createWallet();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.REQUEST_PERMISSION_WRITE_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    createWallet();
                }
                break;
            }
        }
    }

    private void createWallet() {
        String password = passwordView.getText().toString();
        if (!password.isEmpty()) {
            generateWallet(password);
        }else{
            Toast.makeText(this, "Please write a password", Toast.LENGTH_SHORT).show();
        }
    }

    public void generateWallet(final String password) {
        String fileName;
        String walletAddress;
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) {
                path.mkdir();
            }
            fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path)));
            Log.e("app", "generateWallet: " + path + "/" + fileName);

            Credentials credentials = WalletUtils.loadCredentials(password, path + "/" + fileName);
            walletAddress = credentials.getAddress();
            Log.e("app", "generateWallet: " + credentials.getAddress() + " " + credentials.getEcKeyPair().getPublicKey());

            saveWalletData(fileName, password, walletAddress);
            showHome();

        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | IOException
                | CipherException e) {
            e.printStackTrace();
        }
    }

    private void saveWalletData(String fileName, String password, String address) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(Const.PREF_WALLET_ADDRESS, address);
        editor.putString(Const.PREF_WALLET_FILE_NAME, fileName);
        editor.putString(Const.PREF_WALLET_PASSWORD, password);
        editor.apply();
    }

    private void showHome() {
        HomeActivity.start(this);
        finish();
    }
}

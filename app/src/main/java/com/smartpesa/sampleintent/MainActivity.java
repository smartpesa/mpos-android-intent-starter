package com.smartpesa.sampleintent;

import com.smartpesa.intent.SpConnect;
import com.smartpesa.intent.TransactionArgument;
import com.smartpesa.intent.TransactionType;
import com.smartpesa.intent.result.TransactionError;
import com.smartpesa.intent.result.TransactionResult;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private static final int TRANSACTION_REQUEST_CODE = 1001;
    public static final String TAX_1_TYPE = "IVA";
    public static final String TAX_2_TYPE = "IAC";
    private EditText amountEt, tipsEt, tax1Et, tax2Et, externalReferenceEt;
    private FloatingActionButton fab;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        amountEt = (EditText) findViewById(R.id.amount);
        tipsEt = (EditText) findViewById(R.id.tips);
        tax1Et = (EditText) findViewById(R.id.tax1);
        tax2Et = (EditText) findViewById(R.id.tax2);
        result = (TextView) findViewById(R.id.result);
        externalReferenceEt = (EditText) findViewById(R.id.external_reference);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                sendTransaction();
            }
        });
    }

    private void sendTransaction() {
        // Check if SmartPesa App is installed.
        if (SpConnect.isSmartPesaInstalled(this)) {

            // Get amount to be transacted.
            BigDecimal amount = new BigDecimal(amountEt.getText().toString());
            BigDecimal tips = new BigDecimal(tipsEt.getText().toString());
            BigDecimal tax1 = new BigDecimal(tax1Et.getText().toString());
            BigDecimal tax2 = new BigDecimal(tax2Et.getText().toString());
            String externalReference = externalReferenceEt.getText().toString();

            // Create transaction argument.
            TransactionArgument argument = TransactionArgument.builder()
                    .transactionType(TransactionType.SALES)
                    .amount(amount)
                    .tip(tips)
                    .tax1Amount(tax1)
                    .tax1Type(TAX_1_TYPE)
                    .tax2Amount(tax2)
                    .tax2Type(TAX_2_TYPE)
                    .externalReference(externalReference)
                    .build();

            // Create intent from transaction argument.
            Intent intent = SpConnect.createTransactionIntent(argument, false);

            try {
                startActivityForResult(intent, TRANSACTION_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                showSnackbar("SmartPesa was recently uninstalled.");
            }
        } else {
            showSnackbar("SmartPesa is not installed.");
        }
    }

    private void showSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(fab, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TRANSACTION_REQUEST_CODE) {
            if (data == null) {
                // This can happen if SmartPesa was uninstalled or crashed while we're waiting for a
                // result.
                showSnackbar("No result from SmartPesa App.");
                return;
            } else if (resultCode == RESULT_OK) {
                // Parse successful transaction data.
                TransactionResult result = SpConnect.parseSuccessTransaction(data);
                onSuccess(result);
            } else {
                // Parse failed transaction data.
                TransactionError error = SpConnect.parseErrorTransaction(data);
                onFail(error);
            }
        }
    }

    private void onFail(TransactionError error) {
        String errorString = "Error\n" +
                error.transactionException().getMessage() + "\n" +
                error.transactionException().getReason() + "\n";
        if (error.transactionResult() != null) {
            TransactionResult r = error.transactionResult();
            errorString += r.reference() + "\n" +
                    r.reference() + "\n" +
                    r.type() + "\n" +
                    r.responseDescription() + "\n" +
                    r.responseCode() + "\n" +
                    r.datetime() + "\n" +
                    r.currency().symbol() + " " + r.amount() + "\n" +
                    r.card().pan() + ": " + r.card().holderName() + "\n" +
                    r.description() + "\n";
        }
        this.result.setText(errorString);
    }

    private void onSuccess(TransactionResult result) {
        String resultString = "Success\n" +
                result.reference() + "\n" +
                result.type() + "\n" +
                result.responseDescription() + "\n" +
                result.responseCode() + "\n" +
                result.datetime() + "\n" +
                result.currency().symbol() + " " + result.amount() + "\n" +
                result.card().pan() + ": " + result.card().holderName() + "\n" +
                result.description() + "\n";
        this.result.setText(resultString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

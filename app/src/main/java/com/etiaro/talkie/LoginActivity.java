package com.etiaro.talkie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Facebook;
import com.etiaro.facebook.Interfaces;
import com.etiaro.facebook.functions.Login;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Login mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            showProgress(true);

            mAuthTask = new Login(email, password);
            mAuthTask.execute(loginCallbacks);
        }
    }

    ProgressDialog pdialog;
    private void showProgress(boolean show){
        if(show)
            pdialog = ProgressDialog.show(this, "","Logging in, please wait...", true);
        else
            pdialog.cancel();
    }

    private boolean isEmailValid(String login) {
        //login is phone number or email
        return (login.contains("@")|| login.matches("[0-9]+")) && login.length() > 7;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6; //thats the rules of facebook
    }


    Interfaces.LoginCallback loginCallbacks = new Interfaces.LoginCallback(){
        @Override
        public void fail() {
            mAuthTask = null;

            showProgress(false);
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }

        @Override
        public void success(Account ac) {
            mAuthTask = null;
            showProgress(false);

            Log.d("id", "id - "+ ac.getUserID());
            Facebook.getInstance().accounts.put(ac.getUserID(), ac);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(getString(R.string.intent_loggedIn), ac.getUserID());
            startActivity(intent);
            finish();
        }

        @Override
        public void cancelled(){
            mAuthTask = null;
            showProgress(false);
        }
    };
}


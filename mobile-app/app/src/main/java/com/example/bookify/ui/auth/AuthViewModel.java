package com.example.bookify.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.data.model.UserModel;
import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.UserCreationRequest;
import com.example.bookify.data.repository.AuthRepository;
import com.example.bookify.utils.ErrorUtils;

import java.util.Calendar;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final AuthRepository authRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String registerEmail = "";
    private String resetToken = "";

    // Common / Global States
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> apiError = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null); // Backward compatibility
    private final MutableLiveData<UserModel> authSuccessUser = new MutableLiveData<>(null);

    // Business Navigation Events
    private final MutableLiveData<String> unverifiedEmailEvent = new MutableLiveData<>(null);

    // 1. Login Field Errors
    private final MutableLiveData<String> loginUsernameError = new MutableLiveData<>(null);
    private final MutableLiveData<String> loginPasswordError = new MutableLiveData<>(null);

    // 2. Register Field Errors & State
    private final MutableLiveData<String> regUsernameError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regEmailError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regFirstNameError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regLastNameError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regDobError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regCityError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regPasswordError = new MutableLiveData<>(null);
    private final MutableLiveData<String> regConfirmPasswordError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>(false);

    // 3. Forgot Password Field Errors & State
    private final MutableLiveData<String> forgotEmailError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> forgotpPasswordSuccess = new MutableLiveData<>(false);

    // 4. Create / Reset Password Field Errors & State
    private final MutableLiveData<String> resetNewPasswordError = new MutableLiveData<>(null);
    private final MutableLiveData<String> resetConfirmPasswordError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> verifyResetOtpSuccess = new MutableLiveData<>(false);

    // 5. OTP Field Errors & State
    private final MutableLiveData<String> otpError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> verifySuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> resendOtpSuccess = new MutableLiveData<>(false);

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    // --- Getters ---
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getApiError() { return apiError; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<UserModel> getAuthSuccessUser() { return authSuccessUser; }
    public LiveData<String> getUnverifiedEmailEvent() { return unverifiedEmailEvent; }

    // Login Getters
    public LiveData<String> getLoginUsernameError() { return loginUsernameError; }
    public LiveData<String> getLoginPasswordError() { return loginPasswordError; }

    // Register Getters
    public LiveData<String> getRegUsernameError() { return regUsernameError; }
    public LiveData<String> getRegEmailError() { return regEmailError; }
    public LiveData<String> getRegFirstNameError() { return regFirstNameError; }
    public LiveData<String> getRegLastNameError() { return regLastNameError; }
    public LiveData<String> getRegDobError() { return regDobError; }
    public LiveData<String> getRegCityError() { return regCityError; }
    public LiveData<String> getRegPasswordError() { return regPasswordError; }
    public LiveData<String> getRegConfirmPasswordError() { return regConfirmPasswordError; }
    public LiveData<Boolean> getRegisterSuccess() { return registerSuccess; }

    // Forgot Password Getters
    public LiveData<String> getForgotEmailError() { return forgotEmailError; }
    public LiveData<Boolean> getForgotPasswordSuccess() { return forgotpPasswordSuccess; }

    // Reset Password Getters
    public LiveData<String> getResetNewPasswordError() { return resetNewPasswordError; }
    public LiveData<String> getResetConfirmPasswordError() { return resetConfirmPasswordError; }
    public LiveData<Boolean> getResetPasswordSuccess() { return resetPasswordSuccess; }
    public LiveData<Boolean> getVerifyResetOtpSuccess() { return verifyResetOtpSuccess; }

    // OTP Getters
    public LiveData<String> getOtpError() { return otpError; }
    public LiveData<Boolean> getVerifySuccess() { return verifySuccess; }
    public LiveData<Boolean> getResendOtpSuccess() { return resendOtpSuccess; }

    public String getRegisterEmail() { return registerEmail; }
    public void setRegisterEmail(String email) { this.registerEmail = email; }

    // Giuúp reset trạng thái của forgot password (không bị re-trigger khi user chuyển sang verify
    // mà user bấm back nếu kh set lại false nó tự động nhảy sang verify 1l nữa)
    public void resetForgotPasswordState() { forgotpPasswordSuccess.setValue(false); }
    public void resetRegisterState() { registerSuccess.setValue(false); }
    public void resetVerifyState() { verifySuccess.setValue(false); }
    public void resetResendOtpState() { resendOtpSuccess.setValue(false); }
    public void resetResetPasswordState() { resetPasswordSuccess.setValue(false); }
    public void resetUnverifiedEmailEvent() { unverifiedEmailEvent.setValue(null); }
    public void updateLoadingState(boolean loading) { isLoading.setValue(loading); }

    public void resetVerifyResetOtpState() { verifyResetOtpSuccess.setValue(false); }

    public void clearLoginErrors() {
        loginUsernameError.setValue(null);
        loginPasswordError.setValue(null);
        apiError.setValue(null);
        errorMessage.setValue(null);
    }

    public void clearRegisterErrors() {
        regUsernameError.setValue(null);
        regEmailError.setValue(null);
        regFirstNameError.setValue(null);
        regLastNameError.setValue(null);
        regDobError.setValue(null);
        regCityError.setValue(null);
        regPasswordError.setValue(null);
        regConfirmPasswordError.setValue(null);
        apiError.setValue(null);
        errorMessage.setValue(null);
    }

    public void clearForgotPasswordErrors() {
        forgotEmailError.setValue(null);
        apiError.setValue(null);
        errorMessage.setValue(null);
    }

    public void clearResetPasswordErrors() {
        resetNewPasswordError.setValue(null);
        resetConfirmPasswordError.setValue(null);
        apiError.setValue(null);
        errorMessage.setValue(null);
    }

    public void clearOtpErrors() {
        otpError.setValue(null);
        apiError.setValue(null);
        errorMessage.setValue(null);
    }

    // compositeDisposable : obj quản lý nhiều Disposable -> quản lý/hủy nhiều luồng ngầm cùng lúc -> giúp tránh memory leak
    // Khi gọi .subcribe() -> Rxjava trả về 1 Disposable(việc đăng ký đang đc thực hiện , cho phép hủy)
    // subscribeOn(Schedulers.io()) : Khi subscription bắt đầu, công việc sẽ thực hiện trên IO thread(ngầm)
    // observeOn(AndroidSchedulers.mainThread()): xử lý kết quả trả về trên Main thread
    // subscribe() : trả về 1 Disposable -> add vào compositeDisposable -> chạy async:  đăng kí subscription nhận kq từ Single, thực thi Rx chain, chờ kết quả

    // --- Business Methods ---

    public void login(String username, String password) {
        clearLoginErrors();
        boolean isValid = true;

        if (username == null || username.trim().isEmpty()) {
            loginUsernameError.setValue("Please enter your username or email");
            isValid = false;
        } else if (username.contains("@")) {
            if (!EMAIL_PATTERN.matcher(username.trim()).matches()) {
                loginUsernameError.setValue("Please enter a valid email address");
                isValid = false;
            }
        } else if (username.trim().length() < 4) {
            loginUsernameError.setValue("Username must be at least 4 characters");
            isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            loginPasswordError.setValue("Please enter your password");
            isValid = false;
        } else if (password.length() < 6) {
            loginPasswordError.setValue("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) return;

        isLoading.setValue(true);

        compositeDisposable.add(
                authRepository.login(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    isLoading.setValue(false);
                                    authSuccessUser.setValue(user);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    ApiResponse<?> apiResponse = ErrorUtils.parseApiResponse(throwable);
                                    String msg = (apiResponse != null && apiResponse.getMessage() != null)
                                            ? apiResponse.getMessage()
                                            : ErrorUtils.parseError(throwable, "Login failed");
                                    errorMessage.setValue(msg);

                                    if ((apiResponse != null && apiResponse.getCode() == 1035)
                                            || msg.toLowerCase().contains("not verified") || msg.contains("1035")) {
                                        String email = ErrorUtils.extractEmailFromResult(apiResponse);
                                        if (email == null || email.trim().isEmpty()) {
                                            email = username;
                                        }
                                        this.registerEmail = email;
                                        unverifiedEmailEvent.setValue(email);
                                    } else if (msg.toLowerCase().contains("unauthenticated") || msg.toLowerCase().contains("invalid") || msg.contains("1006") || msg.contains("1042")) {
                                        apiError.setValue("Login failed. Please check your credentials.");
                                        loginUsernameError.setValue("Invalid username or password");
                                        loginPasswordError.setValue("Invalid username or password");
                                    } else {
                                        apiError.setValue(msg);
                                    }
                                }
                        )
        );
    }

    public void register(String username, String email, String firstName, String lastName,
                         String dob, String city, String password, String confirmPassword, boolean termsAccepted) {
        clearRegisterErrors();
        boolean isValid = true;

        if (username == null || username.trim().isEmpty()) {
            regUsernameError.setValue("Username cannot be empty");
            isValid = false;
        } else if (username.trim().length() < 4) {
            regUsernameError.setValue("Username must be at least 4 characters");
            isValid = false;
        }

        if (email == null || email.trim().isEmpty()) {
            regEmailError.setValue("Email cannot be empty");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            regEmailError.setValue("Please enter a valid email address");
            isValid = false;
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            regFirstNameError.setValue("First name cannot be empty");
            isValid = false;
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            regLastNameError.setValue("Last name cannot be empty");
            isValid = false;
        }

        if (dob == null || dob.trim().isEmpty()) {
            regDobError.setValue("Please select your date of birth");
            isValid = false;
        } else if (!isAgeAtLeast18(dob)) {
            regDobError.setValue("You must be 18 years or older");
            isValid = false;
        }

        if (city == null || city.trim().isEmpty()) {
            regCityError.setValue("City cannot be empty");
            isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            regPasswordError.setValue("Password cannot be empty");
            isValid = false;
        } else if (password.length() < 8) {
            regPasswordError.setValue("Password must be at least 8 characters");
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            regPasswordError.setValue("Must contain at least 1 lowercase letter");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            regPasswordError.setValue("Must contain at least 1 uppercase letter");
            isValid = false;
        } else if (!password.matches(".*[0-9].*")) {
            regPasswordError.setValue("Must contain at least 1 number");
            isValid = false;
        } else if (!password.matches(".*[^a-zA-Z0-9].*")) {
            regPasswordError.setValue("Must contain at least 1 special character");
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            regConfirmPasswordError.setValue("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            regConfirmPasswordError.setValue("Passwords do not match");
            isValid = false;
        }

        if (!termsAccepted) {
            apiError.setValue("You must agree to the Terms of Service and Privacy Policy");
            isValid = false;
        }

        if (!isValid) return;

        isLoading.setValue(true);
        this.registerEmail = email.trim();

        String formattedDob = formatDob(dob);
        UserCreationRequest request = new UserCreationRequest(
                username.trim(), password, email.trim(), firstName.trim(), lastName.trim(), formattedDob, city.trim()
        );

        compositeDisposable.add(
                authRepository.register(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading.setValue(false);
                                    registerSuccess.setValue(true);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Registration failed");
                                    errorMessage.setValue(msg);
                                    apiError.setValue(msg);
                                    if (msg.toLowerCase().contains("user existed") || msg.contains("1002")) {
                                        regUsernameError.setValue("Username already taken");
                                    } else if (msg.toLowerCase().contains("email") || msg.contains("1009")) {
                                        regEmailError.setValue("Email already registered");
                                    }
                                }
                        )
        );
    }

    private String formatDob(String dobStr) {
        try {
            String[] parts = dobStr.split("/");
            if (parts.length == 3) {
                return String.format("%s-%s-%s", parts[2], parts[1], parts[0]);
            }
        } catch (Exception ignored) {}
        return dobStr;
    }

    private boolean isAgeAtLeast18(String dobStr) {
        if (dobStr == null || dobStr.trim().isEmpty()) return false;
        try {
            String[] parts = dobStr.split("/");
            if (parts.length != 3) return false;
            int day = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim()) - 1;
            int year = Integer.parseInt(parts[2].trim());

            Calendar today = Calendar.getInstance();
            Calendar dob = Calendar.getInstance();
            dob.set(year, month, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public void verifyEmail(String otp) {
        clearOtpErrors();
        if (otp == null || otp.trim().isEmpty() || otp.trim().length() < 4) {
            otpError.setValue("Please enter all 4 OTP numbers");
            return;
        }

        isLoading.setValue(true);

        compositeDisposable.add(
                authRepository.verifyEmail(registerEmail, otp.trim())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isSuccess -> {
                                    isLoading.setValue(false);
                                    verifySuccess.setValue(true);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Verification failed");
                                    errorMessage.setValue(msg);
                                    otpError.setValue(msg);
                                }
                        )
        );
    }

    public void resendOtp() {
        isLoading.setValue(true);
        clearOtpErrors();

        compositeDisposable.add(
                authRepository.resendVerification(registerEmail)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isSuccess -> {
                                    isLoading.setValue(false);
                                    resendOtpSuccess.setValue(true);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Failed to resend OTP");
                                    errorMessage.setValue(msg);
                                    apiError.setValue(msg);
                                }
                        )
        );
    }

    public void forgotPassword(String email) {
        clearForgotPasswordErrors();

        if (email == null || email.trim().isEmpty()) {
            forgotEmailError.setValue("Email cannot be empty");
            return;
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            forgotEmailError.setValue("Please enter a valid email address");
            return;
        }

        isLoading.setValue(true);
        this.registerEmail = email.trim();

        compositeDisposable.add(
                authRepository.forgotPassword(email.trim())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isSuccess -> {
                                    isLoading.setValue(false);
                                    forgotpPasswordSuccess.setValue(true);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Failed to send reset code");
                                    errorMessage.setValue(msg);
                                    apiError.setValue(msg);
                                    forgotEmailError.setValue(msg);
                                }
                        )
        );
    }

    public void verifyResetOtp(String otp) {
        // Reset lỗi cũ
        otpError.setValue(null);
        apiError.setValue(null);

        // Gọi repo để verifyOtp
        isLoading.setValue(true);
        compositeDisposable.add(
                authRepository.verifyResetOtp(registerEmail, otp)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(token -> {
                            isLoading.setValue(false);
                            this.resetToken = token;
                            verifyResetOtpSuccess.setValue(true);
                        }, error -> {
                            isLoading.setValue(false);
                            String msg = ErrorUtils.parseError(error, "Failed to verify OTP");
                            otpError.setValue(msg);
                        })
        );
    }



    public void resetPassword(String newPassword, String confirmPassword) {
        clearResetPasswordErrors();
        boolean isValid = true;

        if (resetToken == null || resetToken.trim().isEmpty()) {
            apiError.setValue("Invalid or missing reset token. Please retry.");
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            resetNewPasswordError.setValue("Password cannot be empty");
            isValid = false;
        } else if (newPassword.length() < 8) {
            resetNewPasswordError.setValue("Password must be at least 8 characters");
            isValid = false;
        } else if (!newPassword.matches(".*[a-z].*")) {
            resetNewPasswordError.setValue("Must contain at least 1 lowercase letter");
            isValid = false;
        } else if (!newPassword.matches(".*[A-Z].*")) {
            resetNewPasswordError.setValue("Must contain at least 1 uppercase letter");
            isValid = false;
        } else if (!newPassword.matches(".*[0-9].*")) {
            resetNewPasswordError.setValue("Must contain at least 1 number");
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            resetConfirmPasswordError.setValue("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            resetConfirmPasswordError.setValue("Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        isLoading.setValue(true);

        compositeDisposable.add(
                authRepository.resetPassword(resetToken, newPassword)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isSuccess -> {
                                    isLoading.setValue(false);
                                    resetPasswordSuccess.setValue(true);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Failed to reset password");
                                    errorMessage.setValue(msg);
                                    apiError.setValue(msg);
                                }
                        )
        );
    }

    public void loginWithGoogle(String idToken) {
        clearLoginErrors();
        isLoading.setValue(true);

        compositeDisposable.add(
                authRepository.loginWithGoogle(idToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    isLoading.setValue(false);
                                    authSuccessUser.setValue(user);
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    String msg = ErrorUtils.parseError(throwable, "Google authentication failed");
                                    errorMessage.setValue(msg);
                                    apiError.setValue(msg);
                                }
                        )
        );
    }
}


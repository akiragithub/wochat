package com.juns.wechat.view.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.MainActivity;
import com.juns.wechat.R;
import com.juns.wechat.common.DES;
import com.juns.wechat.common.Utils;
import com.juns.wechat.net.BaseJsonRes;
import com.juns.wechat.view.BaseActivity;

//注册
public class RegisterActivity extends BaseActivity implements OnClickListener {
	private TextView txt_title;
	private ImageView img_back;
	private Button btn_register, btn_send;
	private EditText et_usertel, et_password, et_username, et_student_id;
	private MyCount mc;
	private static String TAG = "REGISTERACTIVITY";
	private String name;
	private String password;
	private String id;
	private String studentClass;
	private String phone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_register);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initControl() {
		txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText(getString(R.string.register_1));//Previous value "注册"
		img_back = (ImageView) findViewById(R.id.img_back);
		img_back.setVisibility(View.VISIBLE);
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_register = (Button) findViewById(R.id.btn_register);
		et_username = (EditText) findViewById(R.id.et_name);
		et_usertel = (EditText) findViewById(R.id.et_usertel);
		et_password = (EditText) findViewById(R.id.et_password);
		et_student_id = (EditText) findViewById(R.id.et_id);
	}

	@Override
	protected void initView() {
	}

	@Override
	protected void initData() {
	}

	@Override
	protected void setListener() {
		img_back.setOnClickListener(this);
		btn_send.setOnClickListener(this);
		btn_register.setOnClickListener(this);
		et_username.addTextChangedListener(new TextChange());
		et_student_id.addTextChangedListener(new TextChange());
		et_usertel.addTextChangedListener(new TelTextChange());
		et_password.addTextChangedListener(new TextChange());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			Utils.finish(RegisterActivity.this);
			break;
		case R.id.btn_send:
			//TODO : must Be rigorously implemented later
			if (mc == null) {
				mc = new MyCount(60000, 1000); // 第一参数是总的时间，第二个是间隔时间
				//mc = new MyCount(60, 000); // 第一参数是总的时间，第二个是间隔时间
			}
			mc.start();
			checkId();
			break;
		case R.id.btn_register:
			getRegister();
			break;
		default:
			break;
		}
	}

	private void getRegister() {
		//final String name = et_username.getText().toString();
		phone = et_usertel.getText().toString();
		password = et_password.getText().toString();
		if (!Utils.isMobileNO(phone)) {
			Utils.showLongToast(RegisterActivity.this, getString(R.string.enter_a_valid_number_toast));
			return;
		}
		if (TextUtils.isEmpty(password)){
			Utils.showLongToast(RegisterActivity.this, getString(R.string.set_name_and_pwd));
			return;
		}
		getLoadingDialog(getString(R.string.signing_up)).show();
		btn_register.setEnabled(false);
		btn_send.setEnabled(false);
		RequestParams params = new RequestParams();
		params.put("username", name);
		params.put("password", DES.md5Pwd(password));
		// params.put("checkCode", code);
		netClient.post(Constants.RegistURL, params, new BaseJsonRes() {
			@Override
			public void onMySuccess(String data) {
				Utils.putValue(RegisterActivity.this, Constants.UserInfo, data);
				Utils.putValue(RegisterActivity.this, Constants.NAME, name);
				Utils.putValue(RegisterActivity.this, Constants.PWD,
						DES.md5Pwd(password));
				Utils.putBooleanValue(RegisterActivity.this,
						Constants.LoginState, true);
				getChatserive(name, DES.md5Pwd(password));
			}

			@Override
			public void onMyFailure() {
				getLoadingDialog("").dismiss();
				btn_register.setEnabled(true);
				btn_send.setEnabled(true);
			}
		});
	}

	private void getChatserive(final String userName, final String password) {
		EMChatManager.getInstance().login(userName, password, new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						runOnUiThread(new Runnable() {
							public void run() {
								Utils.putBooleanValue(RegisterActivity.this,
										Constants.LoginState, true);
								Utils.putValue(RegisterActivity.this,
										Constants.User_ID, userName);
								Utils.putValue(RegisterActivity.this,
										Constants.PWD, password);
								Log.d("main", "登陆聊天服务器成功！");
								// 加载群组和会话
								EMGroupManager.getInstance().loadAllGroups();
								EMChatManager.getInstance()
										.loadAllConversations();
								getLoadingDialog("正在登录...").dismiss();
								Utils.showLongToast(RegisterActivity.this,
										"注册成功！");
								Intent intent = new Intent(
										RegisterActivity.this,
										EditUserNameActivity.class);
								startActivity(intent);
								overridePendingTransition(R.anim.push_up_in,
										R.anim.push_up_out);
								finish();
							}
						});
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.d("main", "登陆聊天服务器失败！");
						runOnUiThread(new Runnable() {
							public void run() {
								getLoadingDialog("正在注册...").dismiss();
								Utils.showLongToast(RegisterActivity.this,
										"注册失败！");
							}
						});
					}
				});
	}

	private void checkId() {
		name = et_username.getText().toString();
		id = et_student_id.getText().toString();
		RequestParams params = new RequestParams();
		Toast.makeText(context, "Wait while we process your identity", Toast.LENGTH_SHORT).show();
		params.put("name", name);
		params.put("id",id);
		//params.put("telephone", "+22997816156");
		params.put("codeType", "1");
        netClient.post(Constants.checkIdURL, params,
				new JsonHttpResponseHandler() {
					@Override
					protected void handleMessage(Message msg) {
						super.handleMessage(msg);
						Log.d(TAG,"New message to handle : "+msg.toString());
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						super.onFailure(e, errorResponse);
						Log.d(TAG,"Connexion failed : reason : " + e.toString());
					}
					@Override
					public void onSuccess(JSONObject response) {
						super.onSuccess(response);
						Log.d(TAG,"Successful id cheick performed");
						try {
							Log.d(TAG,"OnSuccess started now");
							JSONObject result = response.getJSONObject("results");
							Log.d(TAG,"Resonse from server : " + response.toString());
							//System.out.println("返回的值" + response);
							if (result == null) {
								Utils.showLongToast(App.getInstance(),
										Constants.NET_ERROR);
							} else if (result.getString("is_student").equals("1")) {
								// in the server part is_student value is 1 for true and 0 for false
								String studentClass = result.getString("student_class");
								Utils.showLongToast(App.getInstance(), studentClass);
								btn_register.setEnabled(true);
							} else {
								// in case of wrong id or name;
								Utils.showLongToast(App.getInstance(), getString(R.string.wrong_auth_toast));
								mc.cancel();
								btn_send.setEnabled(true);
								// TODO what is the meaning of the words below ?
								btn_send.setText("发送验证码");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		Log.d(TAG,"netclient finished its task");
	}

    // 手机号 EditText监听器
	class TelTextChange implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence cs, int start, int before,
				int count) {
			phone = et_usertel.getText().toString();
			if (phone.length() >= 10) {
				if (Utils.isMobileNO(phone)) {
					btn_send.setBackgroundResource(R.drawable.btn_bg_green);
					btn_send.setTextColor(0xFFFFFFFF);
					btn_send.setEnabled(true);
					btn_register.setBackgroundResource(R.drawable.btn_bg_green);
					btn_register.setTextColor(0xFFFFFFFF);
					btn_register.setEnabled(true);
				} else {
					et_usertel.requestFocus();
					Utils.showLongToast(context, getString(R.string.enter_a_valid_phone));
				}
			} else {
				btn_send.setBackgroundResource(R.drawable.btn_enable_green);
				btn_send.setTextColor(0xFFD0EFC6);
				btn_send.setEnabled(false);
				btn_register.setBackgroundResource(R.drawable.btn_enable_green);
				btn_register.setTextColor(0xFFD0EFC6);
				btn_register.setEnabled(true);
			}
		}
	}

	// EditText监听器
	class TextChange implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence cs, int start, int before,
				int count) {
			boolean Sign1 = et_username.getText().length() > 0;
			boolean Sign2 = et_student_id.getText().length() > 0;
			boolean Sign3 = et_usertel.getText().length() > 0;
			boolean Sign4 = et_password.getText().length() > 0;

			String id = et_student_id.getText().toString();
			if (id.length() >= 1) {
					btn_send.setBackgroundResource(R.drawable.btn_bg_green);
					btn_send.setTextColor(0xFFFFFFFF);
					btn_send.setEnabled(true);
					btn_register.setBackgroundResource(R.drawable.btn_bg_green);
					btn_register.setTextColor(0xFFFFFFFF);
					btn_register.setEnabled(true);
			} else {
				btn_send.setBackgroundResource(R.drawable.btn_enable_green);
				btn_send.setTextColor(0xFFD0EFC6);
				btn_send.setEnabled(false);
				btn_register.setBackgroundResource(R.drawable.btn_enable_green);
				btn_register.setTextColor(0xFFD0EFC6);
				btn_register.setEnabled(true);
			}
			if (Sign1 & Sign2 & Sign3& Sign4) {
				btn_register.setBackgroundResource(R.drawable.btn_bg_green);
				btn_register.setTextColor(0xFFFFFFFF);
				btn_register.setEnabled(true);
			} else {
				btn_register.setBackgroundResource(R.drawable.btn_enable_green);
				btn_register.setTextColor(0xFFD0EFC6);
				btn_register.setEnabled(false);
			}
		}
	}

	/* 定义一个倒计时的内部类 */
	private class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			btn_send.setEnabled(true);
			btn_send.setText("发送验证码");
		}

		@Override
		public void onTick(long millisUntilFinished) {
			btn_send.setEnabled(false);
			btn_send.setText("(" + millisUntilFinished / 1000 + ")秒");
		}
	}

	private void initUserList() {
		Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
		finish();
	}
}

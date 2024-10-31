from flask import (
    Flask,
    render_template,
    request,
    redirect,
    url_for,
    session,
    flash,
    get_flashed_messages,
)
from db import *
import json

# Flask 앱 설정
app = Flask(__name__)
app.secret_key = "your_secret_key"

# 고정된 로그인 정보
FIXED_USER_ID = "admin"
FIXED_PASSWORD = "aict2024!!"
FIXED_PASSPHRASE = "AICT"


# 메인 페이지: 로그인 폼
@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        id = request.form["id"]
        password = request.form["password"]
        code = request.form["code"]

        # 입력값이 고정된 값과 일치하는지 확인
        if (
            id == FIXED_USER_ID
            and password == FIXED_PASSWORD
            and code == FIXED_PASSPHRASE
        ):
            session["user_id"] = id
            session["is_admin"] = True  # 관리자 권한으로 설정

            get_flashed_messages()

            return redirect(url_for("device_status"))
        else:
            flash("로그인 실패: 아이디, 비밀번호 또는 암구호가 일치하지 않습니다")
            return redirect(url_for("index"))

    return render_template("index.html")


# 기기 상태 조회 페이지
@app.route("/device_status")
def device_status():
    if "user_id" not in session or not session.get("is_admin"):
        return redirect(url_for("index"))
    devices = get_all_device_status()
    return render_template("device_status.html", devices=[dict(row) for row in devices])


# 기기 상태 조회 페이지
@app.route("/status_devices")
def status_devices():
    if "user_id" not in session or not session.get("is_admin"):
        return redirect(url_for("index"))
    devices = get_all_device_status()
    print(devices)
    print(json.dumps([dict(row) for row in devices], indent=4))
    return json.dumps([dict(row) for row in devices], indent=4)


# 삭제 처리 경로 추가
@app.route("/delete_device/<int:device_id>", methods=["POST"])
def delete_device_route(device_id):
    if "user_id" not in session or not session.get("is_admin"):
        return redirect(url_for("index"))

    # 기기 삭제 처리 호출
    delete_device(device_id)

    # 삭제 후 기기 상태 페이지로 리다이렉트
    return redirect(url_for("device_status"))


if __name__ == "__main__":
    create_db()
    app.run(host="0.0.0.0", port=5000, debug=True)

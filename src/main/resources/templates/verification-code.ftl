<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>验证码 - T-Notes</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f9;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .container {
            background-color: #ffffff;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            text-align: center;
            max-width: 400px;
            width: 100%;
        }
        h1 {
            color: #6366f1;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .website-name {
            font-size: 24px;
            font-weight: bold;
            color: #6366f1;
            margin-bottom: 10px;
        }
        .user-email {
            font-size: 16px;
            color: #666666;
            margin-bottom: 20px;
        }
        .code {
            font-size: 32px;
            font-weight: bold;
            color: #6366f1;
            margin: 25px 0;
            padding: 15px;
            background-color: #f0f0ff;
            border-radius: 8px;
            display: inline-block;
        }
        .note {
            color: #666666;
            font-size: 14px;
            margin-top: 20px;
        }
        .footer {
            margin-top: 30px;
            font-size: 12px;
            color: #999999;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="website-name">T-Notes</div>
    <h1>您的验证码</h1>
    <div class="user-email">尊敬的 ${email}，您好！</div>
    <div class="code">${code}</div>
    <p class="note">请勿将此验证码分享给他人。</p>
    <div class="footer">© 2025 notes.t-music.cn All rights reserved.</div>
</div>
</body>
</html>
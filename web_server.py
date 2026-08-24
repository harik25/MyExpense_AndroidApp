import http.server
import socketserver
import socket
import os

PORT = 3000

def get_apk_path():
    candidates = [
        "/app/applet/app/build/outputs/apk/debug/app-debug.apk",
        "/app/applet/app/apk/app-debug.apk",
        "/app/applet/.build-outputs/app-debug.apk"
    ]
    for p in candidates:
        if os.path.exists(p):
            return p
    return None

class APKDownloadHandler(http.server.BaseHTTPRequestHandler):
    def do_HEAD(self):
        apk_path = get_apk_path()
        if self.path in ["/download", "/download/", "/app-debug.apk", "/Bachat-v2.0.apk"]:
            if apk_path and os.path.exists(apk_path):
                file_size = os.path.getsize(apk_path)
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="Bachat-v2.0.apk"')
                self.send_header("Content-Length", str(file_size))
                self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
                self.end_headers()
                return
            else:
                self.send_error(404, "APK File Not Found")
                return

        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()

    def do_GET(self):
        apk_path = get_apk_path()
        
        if self.path in ["/download", "/download/", "/app-debug.apk", "/Bachat-v2.0.apk"]:
            if apk_path and os.path.exists(apk_path):
                file_size = os.path.getsize(apk_path)
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="Bachat-v2.0.apk"')
                self.send_header("Content-Length", str(file_size))
                self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
                self.end_headers()
                try:
                    with open(apk_path, "rb") as f:
                        while chunk := f.read(65536):
                            self.wfile.write(chunk)
                except (ConnectionResetError, BrokenPipeError):
                    pass
                return
            else:
                self.send_error(404, "APK File Not Found")
                return

        file_size_mb = f"{os.path.getsize(apk_path) / (1024 * 1024):.1f}" if apk_path and os.path.exists(apk_path) else "25"
        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download Bachat v2.0 APK</title>
    <style>
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: #0f172a;
            color: #f8fafc;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 24px;
        }}
        .card {{
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 20px;
            padding: 40px 32px;
            max-width: 440px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        }}
        .icon {{
            width: 72px;
            height: 72px;
            background: #10b981;
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 32px;
            font-weight: bold;
            color: white;
            box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.3);
        }}
        h1 {{ font-size: 24px; font-weight: 700; margin-bottom: 8px; color: #ffffff; }}
        p {{ font-size: 14px; color: #94a3b8; margin-bottom: 24px; line-height: 1.5; }}
        .btn {{
            display: inline-block;
            width: 100%;
            padding: 16px;
            background: #10b981;
            color: #ffffff;
            font-weight: 700;
            font-size: 16px;
            border-radius: 12px;
            text-decoration: none;
            transition: all 0.2s;
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
        }}
        .btn:hover {{
            background: #059669;
            transform: translateY(-2px);
        }}
        .info {{
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid #334155;
            display: flex;
            justify-content: space-around;
            font-size: 12px;
            color: #64748b;
        }}
        .info div strong {{ display: block; color: #cbd5e1; font-size: 14px; margin-top: 2px; }}
    </style>
</head>
<body>
    <div class="card">
        <div class="icon">₹</div>
        <h1>Bachat Expense Tracker</h1>
        <p>Version 2.0 • Full Signed Android Package</p>
        <a href="/download" class="btn">Direct Download APK ({file_size_mb} MB)</a>
        <div class="info">
            <div>Size <strong>{file_size_mb} MB</strong></div>
            <div>Version <strong>v2.0</strong></div>
            <div>Format <strong>Signed APK</strong></div>
        </div>
    </div>
</body>
</html>"""
        encoded = html.encode('utf-8')
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

class ThreadingHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True
    allow_reuse_address = True

    def server_bind(self):
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        if hasattr(socket, "SO_REUSEPORT"):
            try:
                self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
            except OSError:
                pass
        super().server_bind()

if __name__ == "__main__":
    server = ThreadingHTTPServer(("0.0.0.0", PORT), APKDownloadHandler)
    print(f"Serving APK download server on port {PORT}")
    server.serve_forever()

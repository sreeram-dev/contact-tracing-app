from ens.app import app

from ens.server_views import IndexView, WarmupView

from ens.diagnosis.views import UploadView, DownloadView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/upload-diagnosis-keys',
                 view_func=UploadView.as_view('upload_view'))
app.add_url_rule('/download-diagnosis-keys',
                 view_func=DownloadView.as_view('download_view'))
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))

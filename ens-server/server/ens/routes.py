from ens.app import app

from ens.base.views import IndexView, WarmupView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))

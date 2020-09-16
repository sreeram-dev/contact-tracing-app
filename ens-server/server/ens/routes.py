from ens.app import app

from ens.verification.views import IndexView, WarmupView
from ens.verification.views import RegistrationView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/register_uuid', view_func=RegistrationView.as_view('register'))
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))

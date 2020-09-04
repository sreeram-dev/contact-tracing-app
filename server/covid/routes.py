from covid.app import app

from covid.verification.views import IndexView, WarmupView
from covid.verification.views import RegistrationView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/register_uuid', view_func=RegistrationView.as_view('register'))
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))

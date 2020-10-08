from covid.app import app

from covid.server_views import IndexView, WarmupView
from covid.profile.views import RegistrationView

from covid.tan.views import RequestTANView, VerifyTANView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/register-uuid',
                 view_func=RegistrationView.as_view('register-uuid'),
                 methods=['POST'])
app.add_url_rule('/request-tan',
                 view_func=RequestTANView.as_view('request-tan'),
                 methods=['POST'])
app.add_url_rule('/verify-tan',
                 view_func=VerifyTANView.as_view('verify-tan'),
                 methods=['POST'])
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))

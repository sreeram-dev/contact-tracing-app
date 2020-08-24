from covid.app import app

from covid.verification.views import IndexView


app.add_url_rule('/', view_func=IndexView.as_view('index'))

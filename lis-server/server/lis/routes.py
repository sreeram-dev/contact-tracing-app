# -*- coding:utf-8 -*-

from lis.app import app

from lis.server_views import IndexView, WarmupView
from lis.patient.views import RegistrationView, DiagnosisView, StatusView


from lis.consent.views import GrantConsentView, RevokeConsentView
from lis.consent.views import AuthenticateConsentView


app.add_url_rule('/', view_func=IndexView.as_view('index'))
app.add_url_rule('/_ah/warmup', view_func=WarmupView.as_view('warmup'))
app.add_url_rule('/register-patient',
                 view_func=RegistrationView.as_view('register-patient'),
                 methods=['POST'])
app.add_url_rule('/set-diagnosis-status',
                 view_func=DiagnosisView.as_view('set-diagnosis-status'),
                 methods=['POST', 'GET'])
app.add_url_rule('/get-patient-status',
                 view_func=StatusView.as_view('status-view'),
                 methods=['GET'])


app.add_url_rule('/grant-consent',
                 view_func=GrantConsentView.as_view('grant-consent'),
                 methods=['POST'])
app.add_url_rule('/revoke-consent',
                 view_func=RevokeConsentView.as_view('revoke-consent'),
                 methods=['POST'])
app.add_url_rule('/authenticate-consent',
                 view_func=AuthenticateConsentView.as_view(
                     'authenticate-consent'),
                 methods=['GET'])

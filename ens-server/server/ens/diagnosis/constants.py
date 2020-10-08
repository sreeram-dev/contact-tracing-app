# -*- coding:utf-8 -*-

from ens.app import APP_MODE

if APP_MODE == 'dev':
    HOST = 'http://localhost:8080'
else:
    HOST = 'https://covidgaurd-285412.ts.r.appspot.com'

VERIFY_TAN_URI = HOST + '/verify-tan'

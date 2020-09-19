import os

from os.path import join
from lis import ROOT_DIR, SOURCE_DIR


class AppConfig(object):
    SECRET_KEY = os.environ.get('SECRET_KEY', 'a6843121fc0b4a3cb81afb4217a5cce8')
    STATIC_FOLDER = join(SOURCE_DIR, 'static')
    TEMPLATE_FOLDER = join(SOURCE_DIR, 'templates')
    LOGGER_NAME = 'prod-logger'
    SERVER_NAME = 'lis-server-289906.ts.r.appspot.com'


class DebugAppConfig(object):
    SECRET_KEY = 'thisismylife'
    STATIC_FOLDER = join(SOURCE_DIR, 'static')
    TEMPLATE_FOLDER = join(SOURCE_DIR, 'templates')
    WTF_CSRF_ENABLED = False
    LOGGER_NAME = 'debug-logger'

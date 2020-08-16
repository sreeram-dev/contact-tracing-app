import os

from os.path import join
from covid import ROOT_DIR, SOURCE_DIR


class AppConfig(object):
    SECRET_KEY = os.environ.get('SECRET_KEY')
    STATIC_FOLDER = join(SOURCE_DIR, 'static')
    TEMPLATE_FOLDER = join(SOURCE_DIR, 'templates')
    LOGGER_NAME = 'prod-logger'
    LOGGER_HANDLER_POLICY = 'always'
    SERVER_NAME = 'sse-poc.herokuapp.com'


class DebugAppConfig(object):
    SECRET_KEY = 'thisismylife'
    STATIC_FOLDER = join(SOURCE_DIR, 'static')
    TEMPLATE_FOLDER = join(SOURCE_DIR, 'templates')
    WTF_CSRF_ENABLED = False
    LOGGER_NAME = 'debug-logger'
    LOGGER_HANDLER_POLICY = 'always'

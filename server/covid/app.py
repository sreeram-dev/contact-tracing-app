# -*- coding:utf-8 -*-

import os
import logging
import logging.config

from os.path import join
from datetime import datetime, timedelta


from flask import Flask, session

from covid.config import AppConfig
from covid.config import DebugAppConfig

from covid import SOURCE_DIR
from covid.logger import LogConfig

logging.config.dictConfig(LogConfig.dictConfig)

app = Flask('CovidGuard-F', template_folder=join(SOURCE_DIR, 'templates'))


APP_MODE = os.environ.get('APP_MODE', 'dev')

if APP_MODE == 'dev':
    app.config['DEBUG'] = True
    app.config['TESTING'] = False
    app.config.from_object(DebugAppConfig)
elif APP_MODE == 'prod':
    app.config['DEBUG'] = False
    app.config['TESTING'] = False
    app.config.from_object(AppConfig)

app.static_folder = join(SOURCE_DIR, 'static')

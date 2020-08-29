# -*- coding:utf-8 -*-

import os
import json
import logging
import logging.config
import werkzeug

from os.path import join
from datetime import datetime, timedelta


from flask import Flask, session
from flask import Flask, request, g
from flask.logging import default_handler
from werkzeug.exceptions import HTTPException

from covid.config import AppConfig
from covid.config import DebugAppConfig

from covid import SOURCE_DIR
from covid.logger import LogConfig

from flask_google_cloud_logger import FlaskGoogleCloudLogger

logging.config.dictConfig(LogConfig.dictConfig)

app = Flask('CovidGuard-F', template_folder=join(SOURCE_DIR, 'templates'))
app.logger.removeHandler(default_handler)

APP_MODE = os.environ.get('APP_MODE', 'dev')

if APP_MODE == 'dev':
    app.config['DEBUG'] = True
    app.config['TESTING'] = False
    app.config.from_object(DebugAppConfig)

elif APP_MODE == 'prod':
    FlaskGoogleCloudLogger(app) # set callbacks
    app.config['DEBUG'] = False
    app.config['TESTING'] = False
    app.config.from_object(AppConfig)

app.logger = logging.getLogger(app.config['LOGGER_NAME'])


@app.teardown_request #log request and response info after extension's callbacks
def log_request_time(_exception):
    if APP_MODE == 'prod':
        app.logger.info(
            f"{request.method} {request.path} - Sent {g.response.status_code}" +
            " in {g.request_time:.5f}ms")


@app.errorhandler(HTTPException)
def handle_exception(e):
    """Return JSON instead of HTML for HTTP errors."""
    # start with the correct headers and status code from the error
    response = e.get_response()
    # replace the body with JSON
    response.data = json.dumps({
        "code": e.code,
        "name": e.name,
        "description": e.description,
    })
    response.content_type = "application/json"
    return response


@app.errorhandler(Exception)
def handle_internal_server_error(e):
    return "Response Failed", 500


app.static_folder = join(SOURCE_DIR, 'static')

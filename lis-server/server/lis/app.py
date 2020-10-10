# -*- coding:utf-8 -*-

import os
import json
import logging
import logging.config
import traceback

from os.path import join
from datetime import datetime, date
from google.api_core.datetime_helpers import DatetimeWithNanoseconds


from flask import Flask
from flask import jsonify, request, g
from flask.json import JSONEncoder
from flask.logging import default_handler
from werkzeug.exceptions import HTTPException

from lis.config import AppConfig
from lis.config import DebugAppConfig

from lis import SOURCE_DIR
from lis.logger import LogConfig

from flask_google_cloud_logger import FlaskGoogleCloudLogger

logging.config.dictConfig(LogConfig.dictConfig)

app = Flask('CovidGuard-F Test Result Server',
            template_folder=join(SOURCE_DIR, 'templates'))
app.logger.removeHandler(default_handler)

APP_MODE = os.environ.get('APP_MODE', 'dev')

if APP_MODE == 'dev':
    app.config['DEBUG'] = True
    app.config['TESTING'] = False
    app.config.from_object(DebugAppConfig)

elif APP_MODE == 'prod':
    FlaskGoogleCloudLogger(app)
    app.config['DEBUG'] = False
    app.config['TESTING'] = False
    app.config.from_object(AppConfig)

app.logger = logging.getLogger(app.config['LOGGER_NAME'])


# log request and response info after extension's callbacks
@app.teardown_request
def log_request_time(_exception):
    if APP_MODE == 'prod':
        app.logger.info(
            f"{request.method} {request.path} - Sent {g.response.status_code}" + " in {g.request_time:.5f}ms")


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

    data = {
        'code': 500,
        'name': e.__class__.__name__,
        'message': str(e)
    }

    data.update({'traceback': traceback.format_exc()})

    return jsonify(data), 500

app.static_folder = join(SOURCE_DIR, 'static')

class CustomJSONEncoder(JSONEncoder):
    def default(self, obj):
        try:
            if (isinstance(obj, date) or isinstance(obj, datetime)
                    or isinstance(obj, DatetimeWithNanoseconds)):
                return obj.isoformat()
            iterable = iter(obj)
        except TypeError:
            pass
        else:
            return list(iterable)
        return JSONEncoder.default(self, obj)

app.json_encoder = CustomJSONEncoder

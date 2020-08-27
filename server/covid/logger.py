# -*- coding:utf-8 -*-

import logging, platform
from collections import OrderedDict

import google.cloud.logging
from google.cloud.logging.handlers import CloudLoggingHandler


client = google.cloud.logging.Client()


class LogConfig(object):
    """Universal logging config
    """

    dictConfig = {
        'version': 1,
        'disable_existing_loggers': False,
        'formatters': {
            "google_json": {
                "()": "flask_google_cloud_logger.FlaskGoogleCloudFormatter",
                "application_info": {
                    "type": "python-application",
                    "application_name": "CovidGuard Application"
                },
                "format": "[%(asctime)s] %(levelname)s in %(module)s: %(message)s"
            },
            'standard': {
                'class': 'jsonformatter.JsonFormatter',
                'format': OrderedDict([
                    ("Name", "name"),
                    ("Levelno", "levelno"),
                    ("Levelname", "levelname"),
                    ("Pathname", "pathname"),
                    ("Filename", "filename"),
                    ("Module", "module"),
                    ("Lineno", "lineno"),
                    ("FuncName", "funcName"),
                    ("Created", "created"),
                    ("Asctime", "asctime"),
                    ("Msecs", "msecs"),
                    ("RelativeCreated", "relativeCreated"),
                    ("Thread", "thread"),
                    ("ThreadName", "threadName"),
                    ("Process", "process"),
                    ("Message", "message")
                ])
            },
            'short': {'format': '%(message)s'}
        },

        'handlers': {

            'console': {
                'class': 'logging.StreamHandler',
                'level': 'DEBUG',
                'formatter': 'standard',
            },

            'google_json': {
                'class': 'logging.StreamHandler',
                'formatter': 'google_json',
            }
        },

        'loggers': {
            'root': {
                'handlers': ['console'],
                'level': 'INFO',
                'propogate': True,
            },

            'prod-logger': {
                'handlers': ['google_json', 'console'],
                'level': 'INFO',
                'propagate': True
            },

            'debug-logger': {
                'handlers': ['console'],
                'level': 'DEBUG',
                'propagate': True
            },

            'werkzeug': {'propagate': True}
        },
    }

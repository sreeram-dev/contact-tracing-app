# -*- coding:utf-8 -*-

import logging, platform

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
            'standard': {'format': '%(asctime)s %(hostname)s: - %(name)s - %(levelname)s - %(message)s - [in %(pathname)s:%(lineno)d]'},
            'short': {'format': '%(message)s'}
        },
        'handlers': {
            'debug': {
                'level': 'DEBUG',
                'formatter': 'standard',
                'class': 'logging.StreamHandler'
            },

            'console': {
                'class': 'logging.StreamHandler',
                'level': 'DEBUG'
            },

            'google_cloud': {
                'class': 'google.cloud.logging.handlers.CloudLoggingHandler',
                'level': 'INFO',
                'formatter': 'standard',
                'client': client,
            }
        },
        'loggers': {
            'prod-logger': {
                'handlers': ['google_cloud', 'debug'],
                'level': 'INFO',
                'propagate': True
            },
            'debug-logger': {
                'handlers': ['debug'],
                'level': 'DEBUG',
                'propagate': True
            },
            'werkzeug': {'propagate': True}
        },
    }

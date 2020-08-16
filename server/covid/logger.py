# -*- coding:utf-8 -*-

import logging, platform


class LogConfig(object):
    dictConfig = {
        'version': 1,
        'disable_existing_loggers': False,
        'formatters': {
            'standard': {'format': '%(asctime)s %(hostname)s: - %(name)s - %(levelname)s - %(message)s - [in %(pathname)s:%(lineno)d]'},
            'short': {'format': '%(message)s'}
        },
        'handlers': {
            'debug-file': {
                'level': 'INFO',
                'formatter': 'standard',
                'class': 'logging.handlers.RotatingFileHandler',
                'filename': 'covid-server.log',
                'maxBytes': 5000000,
                'backupCount': 10
            },
            'debug': {
                'level': 'DEBUG',
                'formatter': 'standard',
                'class': 'logging.StreamHandler'
            },

            'console': {
                'class': 'logging.StreamHandler',
                'level': 'DEBUG'
            },
        },
        'loggers': {
            'prod-logger': {
                'handlers': ['debug'],
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

# -*- coding:utf-8 -*-

from google.cloud import firestore

from ens.app import app
from ens.providers.patterns import Singleton


class FireStoreClient(metaclass=Singleton):
    """Prod firestore client to run applications
    """
    APP_MODE = 'PROD'
    client = None

    @classmethod
    def get_instance(cls):
        if cls._instance.client is None:
            app.logger.info("Initialising firestore client")
            cls._instance.client = firestore.Client()

        return cls._instance

    @classmethod
    def get_client(cls):
        instance = cls.get_instance()
        return instance.client


def get_firestore_client():
    """Get firestore client
    """

    return FireStoreClient.get_client()

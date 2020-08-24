# -*- coding:utf-8 -*-

from google.cloud import firestore

from covid.app import app
from covid.providers.patterns import Singleton


class FireStoreClient(Singleton):
    """Prod firestore client to run applications
    """
    APP_MODE = 'PROD'

    def __init__(self, *args, **kwargs):
        self.client = firestore.Client()

    @classmethod
    def get_instance(cls):
        return cls._instance

    @classmethod
    def get_client(cls):
        return cls._instance.client


def get_firestore_client():
    """Get firestore client
    """

    return ProdFireStoreClient.get_instance().get_client()

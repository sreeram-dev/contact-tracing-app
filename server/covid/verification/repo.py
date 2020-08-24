# -*- coding:utf-8 -*-


from covid.app import APP_MODE
from covid.providers.db import FireStoreClient
from covid.utils import get_collection_name
from covid.constants import TOKEN_COLLECTION


class TokenRepository(object):
    """Token Repository to store registered tokens and corresponding UUIDs
    """
    client = FireStoreClient.get_client()

    def find_by_uuid(self, uuid):
        """Get token based on uuid
        """
        collection = self.client.collection(
                get_collection_name(TOKEN_COLLECTION))
        document = collection.get({'uuid': uuid})
        return document


    def find_by_token(self, token):
        """Get uuid based on token
        """
        collection = self.client.collection(
                get_collection_name(TOKEN_COLLECTION))
        document = collection.get({'token': token})
        return document


    def insert(self, uuid, token):
        collection = self.client.collection(
                get_collection_name(TOKEN_COLLECTION))

        collection.add({'token': token, 'uuid': uuid})
        retun

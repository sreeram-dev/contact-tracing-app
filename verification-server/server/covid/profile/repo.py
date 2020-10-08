# -*- coding:utf-8 -*-

from covid.profile.models import UserProfile


class TokenRepository(object):
    """Token Repository to store registered tokens and corresponding UUIDs
    Repositories are different from DAOs
    """

    def find_by_uuid(self, uuid: str) -> UserProfile:
        """Get token based on uuid
        """
        return UserProfile.collection.filter('uuid', '==', uuid).get()

    def find_by_token(self, token: str) -> UserProfile:
        """Get uuid based on token
        """
        key = UserProfile.get_key_with_namespace(token)
        return UserProfile.collection.get(key)

    def insert(self, uuid: str, token: str) -> UserProfile:
        """Insert the uuid and token in the firestore database
        """
        profile = UserProfile.collection.create(uuid=uuid, token=token)
        return profile

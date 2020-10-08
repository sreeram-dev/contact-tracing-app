# -*- coding:utf-8 -*-

from fireo.models import Model
from fireo.fields import IDField, TextField, BooleanField, DateTime
from covid.utils import get_collection_name


class UserProfile(Model):
    """Anonymous User Profile to be used for creating the DB
    """
    token = IDField(column_name='token', required=True)
    uuid = TextField(column_name='uuid', max_length=32, required=True)
    is_positive = BooleanField(column_name='is_positive', default=False)
    created_at = DateTime(column_name='created_at', auto=True)

    class Meta:
        collection_name = get_collection_name('user_profile')

    @classmethod
    def get_key_with_namespace(cls, token: str) -> str:
        key = cls._meta.collection_name + "/" + token
        return key

# -*- coding:utf-8 -*-


from fireo.models import Model
from fireo.fields import TextField, DateTime
from fireo.fields import BooleanField
from covid.utils import get_collection_name


class TANInfo(Model):
    """TAN for retrieving fields from a user
    """
    tan = TextField(column_name='tan', max_length=16, required=True)
    expired_at = DateTime(column_name='expired_at', required=True)
    token = TextField(column_name='token', max_length=32, required=True)
    is_verified = BooleanField(column_name='is_verified', default=False)

    class Meta:
        collection_name = get_collection_name('tan_info')

    @classmethod
    def get_key_with_namespace(cls, token: str) -> str:
        key = cls._meta.collection_name + "/" + token
        return key

    @staticmethod
    def get_hardcoded_tans():
        """Always return true for upload
        """
        return [
            "abcd-edfg-hijk-lmnop",
            "123-567-890"
        ]

    def get_tan(self) -> str:
        return self.tan

    def get_expired_at(self) -> DateTime:
        return self.expired_at

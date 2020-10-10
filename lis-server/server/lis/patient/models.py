# -*- coding:utf-8 -*-

from fireo.models import Model

from fireo.fields import TextField
from fireo.fields import DateTime, BooleanField
from lis.utils import get_collection_name


class PatientProfile(Model):
    """PatientProfile to store the patient rate
    """
    uuid = TextField(column_name='uuid', required=True)
    is_positive = BooleanField(column_name='is_positive',
                               default=False)
    positive_at = DateTime(column_name='positive_at')
    is_recovered = BooleanField(column_name='is_recovered',
                                default=False)
    recovered_at = DateTime(column_name='recovered_at')
    created_at = DateTime(column_name='created_at', auto=True)

    class Meta:
        collection_name = get_collection_name('patient')

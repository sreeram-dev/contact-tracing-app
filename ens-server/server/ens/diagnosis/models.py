# -*- coding:utf-8 -*-

from fireo.models import Model
from fireo.fields import IDField, NumberField, TextField
from fireo.fields import DateTime
from ens.utils import get_collection_name


class TEKInfo(Model):
    """TEKInfo
    """
    id = IDField(column_name='id', required=True)
    tek = TextField(column_name='tek', required=True)
    en_interval_number = NumberField(
        column_name='en_interval_number',
        required=True)
    created_at = DateTime(auto=True)

    class Meta:
        collection_name = get_collection_name('tek_info')

# -*- coding:utf-8 -*-

from fireo.models import Model
from fireo.fields import IDField, NumberField
from fireo.fields import DateTime
from ens.utils import get_collection_name


class TEKInfo(Model):
    """TEKInfo
    """
    tek = IDField()
    en_interval_number = NumberField()
    created_at = DateTime(auto=True)

    class Meta:
        collection_name = get_collection_name('tek_info')

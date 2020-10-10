# -*- coding:utf-8 -*-


from ens.app import APP_MODE
from ens.constants import CLIENT_RPI_INTERVAL


def get_collection_name(collection):
    """Get collection name
    """
    mode = APP_MODE.lower()
    if mode.lower() == 'prod':
        return collection
    return mode.lower() + '_' + collection


def get_enin(timestamp: int) -> int:
    return timestamp // (60*CLIENT_RPI_INTERVAL)

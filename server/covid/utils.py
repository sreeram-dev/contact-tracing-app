# -*- coding:utf-8 -*-


from covid.app import APP_MODE


def get_collection_name(collection):
    """Get collection name
    """
    mode = APP_MODE.lower()
    if mode.lower() == 'prod':
        return collection
    return mode.lower() + '_' + collection

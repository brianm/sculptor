#!/bin/sh

sculptor agent deploy --start --name $1 http://static.giftudi.com/$1.tar.gz

import json
import sys

if __name__ == '__main__':
    obj = json.load(sys.stdin, encoding='utf-8')

    with open('a.json', encoding='utf-8') as f:
        obj2 = json.load(f, encoding='utf-8')

    obj.update(obj2)

    print(json.dumps(obj))

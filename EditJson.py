import json


def my_main():
    with open('backup.json') as json_data:
        d = json.load(json_data)
    output = open("backup.json", "w")
    output.write(json.dumps(d))

    print("\u20ac €")


if __name__ == '__main__':
    my_main()

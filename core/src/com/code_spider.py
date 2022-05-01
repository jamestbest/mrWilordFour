import glob

__success_counter = 0
__fail_counter = 0


def write_to_master(file_extension):
    global __success_counter
    global __fail_counter

    this_file = __file__.split("/")
    r = ""
    for i in this_file[:-1]:
        r += i + "/"

    master = open(f"{r}code.txt", "a")

    for file_name in glob.iglob(f'{r}**/*{file_extension}', recursive=True):
        master.write(file_name + "\n\n")
        try:
            f = open(file_name, "r")
        except PermissionError:
            master.write(f"Failed to read {file_name}\n")
            print(f"Failed to read {file_name}")
            __fail_counter += 1
            continue
        try:
            master.write(f.read() + "\n\n")
            __success_counter += 1
            print(f"Successfully added {file_name}")
        except UnicodeDecodeError:
            print(f"File {file_name} not in unicode, failed to decode")
            __fail_counter += 1
        f.close()

    master.close()


if __name__ == '__main__':
    write_to_master(".java")
    write_to_master(".glsl")
    print(f"Successfully added {__success_counter} files to code.txt, failed to read {__fail_counter} files.")
    input("Press ENTER to close")

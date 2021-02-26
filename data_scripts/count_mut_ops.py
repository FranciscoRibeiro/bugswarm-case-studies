import csv
import os

def get_mut_ops(fields):
    mut_ops = list(map(lambda x: x.strip(), fields[-8][1:-1].split(",")))
    if(len(mut_ops) == 1 and mut_ops[0] == ''):
        return []
    else:
        return mut_ops

def sort_by_value(dictionary):
    return sorted(dictionary.items(), key=lambda entry: entry[1], reverse=True)

def print_count(total_mut_ops, mut_ops_per_bug, without_mut_ops):
    print("Total occurrences\n")
    for mut_op, occurrences in sort_by_value(total_mut_ops):
        print(mut_op, occurrences)
    print("\n-----------------------------------------------\n")
    
    print("Occurrences per bug\n")
    for mut_op, buggy_occurrences in sort_by_value(mut_ops_per_bug):
        print(mut_op, buggy_occurrences)
    
    print("\n-----------------------------------------------\n")
    
    print("Buggy versions with ZERO mut ops: ", without_mut_ops)


inferred_dir = "inferred" #Directory containing the inferred mutation operators
total_mut_ops = {} #Occurrences of each mut op
mut_ops_per_bug = {} #Number of buggy versions that contain each mut op
without_mut_ops = 0 #Number of buggy versions without any inferred mutation operators

for root, dirs, filenames in os.walk(inferred_dir):
    for name in filenames:
        mut_ops = set() #Flags the occurrence of a mut op in this buggy version

        f = open(inferred_dir + "/" + name, "r")
        csv_reader = csv.reader(f, delimiter=";")
        next(csv_reader) #Ignore header

        for fields in csv_reader:
            if(not fields[0].startswith("Error,") and not fields[0].startswith("Timeout,")):
                mut_ops_line = get_mut_ops(fields)
                for op in mut_ops_line:
                    mut_ops.add(op)
                    if(op in total_mut_ops):
                        total_mut_ops[op] += 1
                    else:
                        total_mut_ops[op] = 1

        for op in mut_ops:
            if(op in mut_ops_per_bug):
                mut_ops_per_bug[op] += 1
            else:
                mut_ops_per_bug[op] = 1

        if(len(mut_ops) == 0): without_mut_ops += 1

print_count(total_mut_ops, mut_ops_per_bug, without_mut_ops)


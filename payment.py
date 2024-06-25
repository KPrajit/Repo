##initial setting (database)
import mysql.connector
con_o=mysql.connector.connect(host="localhost",user="root",password="",database="test")
cur_o=con_o.cursor()
###actual code (querying)

#description of table
q1="desc payment"
cur_o.execute(q1)
table=cur_o.fetchall()
print("\n\tTable Description")
for atr in table:
    print(atr)
print("\n\t---------------------end----------------\n")
#
a=input("Name: ")
b=float(input("salary: "))
c=int(input("Years of Experience: "))
d=int(input("Deduction Amount: "))
e=b-d
#insert code
q2=f"INSERT INTO `payment`(`Name`, `Salary`, `Years of Experience`, `Deduction Amount`,`Final Salary`) VALUES ('{a}',{b},{c},{d},{e})"
cur_o.execute(q2)
con_o.commit()
#
    
#showing content
print("\n\tRecords in the table")
q3="SELECT * from payment"
cur_o.execute(q3)
rows=cur_o.fetchall()
print(*rows,sep="\n")
print("\n\t---------------------end----------------\n")
# 

##ending steps
cur_o.close()
con_o.close()
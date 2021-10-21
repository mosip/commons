import xlrd
import csv
import os 
for file in os.listdir("xlsx/"):
	if file.endswith(".xlsx"):
		print(file)
		print(os.path.join(os.path.splitext(file)[0]+ ".csv"))
		with xlrd.open_workbook(file) as wb:
			sh = wb.sheet_by_index(0)  # wb.sheet_by_name('sheet_name')
			with open(os.path.join("dml"+os.path.splitext(file)[0]+ ".csv"), 'w', encoding="utf-16", newline="") as f:
				col = csv.writer(f)
				for row in range(sh.nrows):
					col.writerow(sh.row_values(row))

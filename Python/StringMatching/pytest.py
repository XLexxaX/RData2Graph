import random
import multiprocessing as mp
import StringMatcher_Interface as smi

def cpu_heavy(x, q):
	print('Starting')
	
	print("Done")

if __name__ =='__main__':
	procs = 4
	jobs = list()
	queues = list()
	for i in range(0, procs):
		ctx = mp.get_context('spawn')
		q = ctx.Queue()
		process = ctx.Process(target=cpu_heavy, args=('hf', q))
		jobs.append(process)
		queues.append(q)
	for j in jobs:
		j.start()
	for q in queues:
		print(q.get())
	for j in jobs:
		j.join()
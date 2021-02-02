// Monitor
class LE {
  private int leit, escr;  
  
  // Construtor
  LE() { 
     this.leit = 0; //leitores lendo (0 ou mais)
     this.escr = 0; //escritor escrevendo (0 ou 1)
  } 
  
  // Entrada para leitores
  public synchronized void EntraLeitor (int id) {
    try { 
      while (this.escr > 0) {
      //if (this.escr > 0) {
         System.out.println ("le.leitorBloqueado("+id+")");
         wait();  //bloqueia pela condicao logica da aplicacao 
      }
      this.leit++;  //registra que ha mais um leitor lendo
      System.out.println ("le.leitorLendo("+id+")");
    } catch (InterruptedException e) { }
  }
  
  // Saida para leitores
  public synchronized void SaiLeitor (int id) {
     this.leit--; //registra que um leitor saiu
     if (this.leit == 0) 
           this.notify(); //libera escritor (caso exista escritor bloqueado)
     System.out.println ("le.leitorSaindo("+id+")");
  }
  
  // Entrada para escritores
  public synchronized void EntraEscritor (int id) {
    try { 
      while ((this.leit > 0) || (this.escr > 0)) {
      //if ((this.leit > 0) || (this.escr > 0)) {
         System.out.println ("le.escritorBloqueado("+id+")");
         wait();  //bloqueia pela condicao logica da aplicacao 
      }
      this.escr++; //registra que ha um escritor escrevendo
      System.out.println ("le.escritorEscrevendo("+id+")");
    } catch (InterruptedException e) { }
  }
  
  // Saida para escritores
  public synchronized void SaiEscritor (int id) {
     this.escr--; //registra que o escritor saiu
     notifyAll(); //libera leitores e escritores (caso existam leitores ou escritores bloqueados)
     System.out.println ("le.escritorSaindo("+id+")");
  }
}

class Tbuffer
{
	private int[] vec;
	private boolean[] ver;
	private int size,elements;
	Tbuffer(int _size)
	{
		this.vec = new int[_size];
		this.ver = new boolean[_size];
		for(int i = 0; i < _size; i++)
			this.ver[i] = false;
		this.size = _size;
		this.elements = 0;
	}	
	public boolean is_pos_empty(int index)
	{
		return this.ver[index];
	}
	public boolean is_full()
	{
		if(this.elements < this.size) return false;
		return true;
	}
	public boolean is_empty()
	{
		if(this.elements == 0) return true;
		return false;
	}
	
	
	
	public synchronized void set(int index, int value,int t_id) throws Exception
	{
		if(this.ver[index]) throw new Exception("set em posicao cheia, pos: " + index);
		try
		{
			while(is_full())
			{
				System.out.println ("le.escritorBloqueado("+t_id+")");
				wait();
			}
		}catch(InterruptedException e){}
		this.vec[index] = value;
		this.ver[index] = true;
		this.elements++;
	}
	public synchronized int get(int index,int t_id) throws Exception
	{
		if(!this.ver[index]) throw new Exception("get em posicao vazia, pos: " + index);
		try
		{
			while(is_empty())
			{
				System.out.println ("le.leitorBloqueado("+t_id+")");
         			wait();
			}
		}catch(InterruptedException e){}
		this.ver[index] = false;
		this.elements--;
		return this.vec[index];
	}
	public int size()
	{
		return this.size;
	}
	public void print()
	{
		System.out.println("'''valores do buffer:");//comentario em python
		for(int i = 0; i < this.vec.length; i++)
		{
			if(this.ver[i]) System.out.println("buffer["+i+"] = " + this.vec[i]);
			else System.out.println("buffer["+i+"] = 'empty'");
		}
		System.out.println("'''");
	}
}

class Consumidor extends Thread
{
	private int id;
	private Tbuffer buffer;
	private LE monitor;
	Consumidor(int id , Tbuffer _buffer, LE _monitor)
	{
		this.id = id;
		this.buffer = _buffer;
		this.monitor = _monitor;
	}
	
	public void run()//leitura
	{
		this.monitor.EntraLeitor(this.id);
		try
		{
			for(int i = 0; i < this.buffer.size();i++)
			{
				if(!buffer.is_pos_empty(i)) buffer.get(i,this.id);
			}
		}catch(Exception e){}
		this.monitor.SaiLeitor(this.id);
	}
}
class Produtor extends Thread
{	
	private int id;
	private Tbuffer buffer;
	private LE monitor;
	Produtor(int id , Tbuffer _buffer, LE _monitor)
	{
		this.id = id;
		this.buffer = _buffer;
		this.monitor = _monitor;
	}
	
	public void run()
	{
		this.monitor.EntraEscritor(this.id);
		try
		{
			for(int i = 0; i < this.buffer.size();i++)
			{
				if(!buffer.is_pos_empty(i))
				{
					buffer.set(i,this.id,this.id);
					break;
				}
			}
		}catch(Exception e){}
		this.monitor.SaiEscritor(this.id);
	}
}

class Lab
{
	public static void main(String[] args)
	{
		final int n_con = 30,n_prod = 20;
		Tbuffer buffer = new Tbuffer(10);
		LE monitor = new LE();
		Consumidor[] cons = new Consumidor[n_con];
		Produtor[] prods = new Produtor[n_prod];
		System.out.println ("import verificaLE");
		System.out.println ("le = verificaLE.LE()");
		for(int i = 0; i < n_con; i++)
			cons[i] = new Consumidor(i,buffer,monitor);
		for(int i = 0; i < n_prod; i++)
			prods[i] = new Produtor(i,buffer,monitor);
		for(int i = 0; i < n_prod; i++)
			prods[i].start();
		for(int i = 0; i < n_con; i++)
			cons[i].start();
		try{
		for(int i = 0; i < n_prod; i++)
			prods[i].join();
		for(int i = 0; i < n_con; i++)
			cons[i].join();
		buffer.print();
		}catch(InterruptedException e){}			
	}
}





























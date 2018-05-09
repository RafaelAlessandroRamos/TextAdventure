import java.io.File
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

//Comandos: 
//inventory: Mostrar itens
//use x: user item, ex: use porta (quando sem necessidade de chave)
//use x with y: Usar item do inventorio em um item do ambiente, ex: use chave with porta
//check x: Descrever objeto
//get x: Obter objeto(Ao pegar um item ele é copiado ao inventory, e o objeto na cena é dado como -1)

//Comandos do sistema:
//help: Instruções do jogo
//save nome: salvar cena,objetos do inventário e estados de todas as cenas
//load nome: Carregar
//newgame: Começar novo jogo ou reiniciar

public class OJogo {
    Jogo jogo

    def OJogo() {
    	Jogo jogo = new Jogo()
    }

    

    static def exportarJogoJson(Jogo objetoImportado,String nomeArquivoDestino, int sg) {
        if (sg==1){
            nomeArquivoDestino+=".json"
        }
    	def saida = new JsonBuilder(objetoImportado).toPrettyString()
        new File(nomeArquivoDestino).write(saida)
        return true
    }

    static void menu() {
        println("Bem vindo ao jogo Zabuza Momochi")
        print("\n")
    	help()
    }

    static void help() {
    	println "Comandos:\nhelp: Instrucoes do jogo\t\t\tsave nomeDoSave : Salvar jogo\nload nomeDoSave : Carregar jogo salvo\t\tnewgame : Iniciar ou resetar o jogo\nexit : Sair do jogo\t\t\t\tinventory: Mostrar itens\nuse x: usar item, ex: porta\t\t\tuse x with y: Usar item do inventorio em um item do ambiente\ncheck x: Descrever objeto\t\t\tget x: Obter objeto\n"
    }

    static void executarComando(def entrada, Jogo jogo, int tentativas) {	
    	def valores = entrada.split(" ")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        if(valores.length == 1) {
            if(valores[0]=="objetos") {
                jogo.objetos()
            }
            else if(valores[0]=="help") {
                help()
            }
            else if(valores[0]=="newgame") {
                
                if(tentativas==0) {
                    jogo.novoJogo()
                    if(jogo.cenas[jogo.cena_atual].som != null) {
                        jogo.tocarSom(jogo.cenas[jogo.cena_atual].som)
                    }
                    
                } else {
                    println("Deseja reiniciar o jogo?")
                    println("s: para sim")
                    println("n: para não")
                    def confirmacao = br.readLine()
                    if(confirmacao == "s") {
                        jogo.novoJogo()
                        println("Jogo reiniciado")
                    }
                    else if(confirmacao == "n") {
                        println("Jogo não reiniciado")
                    }
                }
            }
            else if(valores[0]=="exit") {
                println("Jogo encerrado")
                jogo.encerrarSons()
                System.exit(0)
            }
            else if(valores[0]=="inventory") {
                jogo.mostrarItens()
            }
            else {
                println("Comando inválido")
            }
        }
        else if (valores.length > 1) {
            if(valores[0]=="check") {
                jogo.checarItem(valores[1])
            }
            else if(valores[0]=="get") {
                jogo.pegarItem(valores[1])
            }
            else if(valores[0]=="save") {
                if(exportarJogoJson(jogo, valores[1],1)) {
                    println("Jogo salvo com sucesso!")
                }
                else {
                    println("Ocorrou algum erro ao salvar o jogo")
                }
            }
            else if(valores[0]=="load") {
                jogo.carregarJogo(valores[1])
            }
            else if(valores[0]=="use") {
                if((valores.length == 4) && (valores[2]=="with")) {
                    jogo.combinarItem(valores[1], valores[3])
                }
                else {
                    jogo.usarItem(valores[1])
                }
            }
            else {
                println("Comando inválido")
            }
        }
    	else {
    		println("Comando inválido")
    	}
    }

    static void loopGame(BufferedReader br,Jogo jogo) {
        Cena cenaAtual = jogo.cenas[jogo.cena_atual]
        int cenaAnterior = jogo.cena_atual
        int tentativas = 0
        String somMenu = "Promise_Reprise"
        //Música do menu
        jogo.tocarSom(somMenu)
        while(1) {
            if((jogo.cena_atual!=cenaAnterior)&&(tentativas>0)){
                cenaAtual = jogo.cenas[jogo.cena_atual]
                if(cenaAtual.som != null) {
                    jogo.tocarSom(cenaAtual.som)
                }
                if(tentativas==0) {
	                println((cenaAtual.id+1) + ". " + cenaAtual.titulo)
	                println(cenaAtual.descricao)
	            }
                cenaAnterior = jogo.cena_atual
            }
            
            
            print("Comando: ")
            def entrada = br.readLine()
            executarComando(entrada,jogo,tentativas)
            println("------------------------------------")
            
            tentativas++
        }
    }

    static void loopC(Jogo jogo) {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
    	loopGame(br,jogo)
    }

    static void main(String[] args) {
    	Jogo jogo = new Jogo(Jogo.importarJogoJson(0))

		menu()
		loopC(jogo)
    }
}

class Objeto {
    //Para referencia no save game
    int id
    // SCENE_OBJECT = 0, objeto de interação da cena. Pode ser aplicado um
    // INVENTORY_OBJECT sobre ele para solucionar puzzle
    // INVENTORY_OBJECT = 1, pode ser obtido pelo comando "get" e
    // vai para o inventário ("obtido" para a ser true)
    int tipo
    //Nome do objeto
    String nome
    //Descricao do objeto
    String descricao
    //Exibido após uso do comando correto
    String resultado_positivo
    //Exibe que o comando foi incorreto
    String resultado_negativo
    //Comando Correto para a solução, ex: check PORTA,use,get x with y
    String comando_correto
    //Indice da cena caso "resultado_positivo" for usado, -1 se levar a nenhum lugar
    int cena_alvo
    //Indica se um comando anterior já o resolveu, ex chave em uma porta
    int resolvido
    //Objeto já foi obtido da cena
    //Se usuário executa comando get OBJECT e obtido == -1 então o objeto já foi coletado e está no inventário (ou já foi usado)
    int obtido
    // flag pra avisar se o objeto estava bloqueado
    boolean comando_travado
    // referencia para qual outro objeto(um terceiro) sofrerá com a ação realizada nesse primeiro objeto
    int id_objeto_destravar
    static Objeto copiarObjeto(def antigo, int idNovo) {
        Objeto novo = new Objeto()
        novo.id = idNovo
        novo.tipo = antigo.tipo
        novo.nome = antigo.nome
        novo.descricao = antigo.descricao
        novo.resultado_positivo = antigo.resultado_positivo
        novo.resultado_negativo = antigo.resultado_negativo
        novo.comando_correto = antigo.comando_correto
        novo.cena_alvo = antigo.cena_alvo
        novo.resolvido = antigo.resolvido
        novo.obtido = antigo.obtido
        novo.comando_travado = antigo.comando_travado
        novo.comando_travado = antigo.comando_travado
        return novo
    }
}

class Cena {
    //Indice da Cena
    int id
    //Titulo da Cena
    String titulo
    //Descricao da Cena
    String descricao
    //Itens disponíveis na cena
    List<Objeto> itens
    //Som da cena
    String som
    def pegarInformacoes() {
        println("Cena ${id}: ${titulo}")
        println(descricao)
    }
}

class Jogo {
    //Indice da cena
    int cena_atual
    //Inventario
    Inventario inventario
    //Vetor de cenas
    List<Cena> cenas 
    //Processo
    private Process processo
    def Jogo() {
        this.cena_atual = 0
        this.inventario = []
        this.cenas = []
    }
    def Jogo(Jogo jogo) {
        this.cena_atual = jogo.cena_atual
        this.inventario = jogo.inventario
        this.cenas = jogo.cenas
    }
    static def importarJogoJson(int sg = 0,String nomeArquivo = "jogo.json") {
        if(sg==1) {
            nomeArquivo+=".json"
        }
        File arquivo = new File(nomeArquivo)
        def oJsonDoArquivo = arquivo.getText()
        def jsonSlurper = new JsonSlurper()
        Jogo objetoImportado = jsonSlurper.parseText(oJsonDoArquivo);
        if(sg==1) {
            println("Jogo carregado com sucesso")
        }
        return objetoImportado;
    }
    def printDescricaoCena()
    {
        println((this.cenas[this.cena_atual].id+1) + ". " + this.cenas[this.cena_atual].titulo)
        println(this.cenas[this.cena_atual].descricao)
    }
    def tocarSom(String nomeSom) {
        if(nomeSom != null) {
            def temp;
            if((nomeSom.contains("http"))||(nomeSom.contains("https"))) {
                temp = "cvlc " + nomeSom + " --loop";
            } else {
                temp = "cvlc sons/" + nomeSom + ".wav --loop";
            }
            //Se já tiver um processo em execução mata ele
            if(this.processo != null) {
                this.processo.waitForOrKill(1);
            }
            this.processo = temp.execute();
        }
        
    }
    def encerrarSons() {
        this.processo.waitForOrKill(1);
    }
    def novoJogo() {
        Jogo temp = importarJogoJson()
        this.cena_atual = temp.cena_atual
        this.inventario = temp.inventario
        this.cenas = temp.cenas
        printDescricaoCena()
    }
    def mostrarItens() {
        this.inventario.each {
            println(it.itens.nome)
        }
    }
    def checarItem(def item) {
        boolean achou = false;
        this.cenas[this.cena_atual].itens.each{itm1 ->
            if(itm1.nome == item) {
                if(itm1.id_objeto_destravar!=null) {
                    this.cenas[this.cena_atual].itens.each{ itm2 ->
                        if(itm1.id_objeto_destravar==itm2.id) {
                            itm2.comando_travado = false
                        }
                    }
                }
                println("Descrição do " + itm1.nome + " : " + itm1.descricao)
                achou = true;
            }
        }
        if(achou == false)
        {
            println("Item não encontrado.")
        }
    }
    def pegarItem(def item) {
        boolean achou = false
        this.cenas[this.cena_atual].itens.each{
            if(it.nome == item) {
            	if( it.obtido == 0 ) {
	                // INVENTORY_OBJECT = 1
	                //Se o comando é correto e se o objeto é INVENTORY_OBJECT
	                if( (it.comando_correto == ("get " + it.nome)) && (it.tipo == 1) ) {
	                    it.obtido = -1
	                    Objeto temp = Objeto.copiarObjeto(it,this.inventario.indice++)
	                    this.inventario.itens.add(temp)
	                    println(it.resultado_positivo)
	                }
	                else {
	                    println(it.resultado_negativo==null ? "Comando inválido" : it.resultado_negativo )
	                }
	                achou = true;
	            }
            }
        }
        if(achou == false) {
            println("Item não encontrado")
        }
    }
    def carregarJogo(def nomeSave) {
        Jogo temp = importarJogoJson(1,nomeSave)
        this.cena_atual = temp.cena_atual
        this.inventario = temp.inventario
        this.cenas = temp.cenas
        printDescricaoCena()
    }


    def objetos(){
        this.cenas[this.cena_atual].itens.each{
            println(it.nome + ", obtido? " + it.obtido + " id: " + it.id)
        }
    }
    def usarItem(def item) {
        boolean achou = false;
        this.cenas[this.cena_atual].itens.each{ itm1 ->
            //Se SCENE_OBJECT e o nome está correto
            if( (itm1.tipo == 0) && (itm1.nome == item) ) {
                if(itm1.comando_correto == ("use " + itm1.nome)) {
                    if(itm1.comando_travado==true) {
                        println(itm1.resultado_negativo==null ? "Comando inválido" : itm1.resultado_negativo )
                    } else {
                        if(itm1.id_objeto_destravar!=null) {
                            this.cenas[this.cena_atual].itens.each{ itm2 ->
                                if(itm1.id_objeto_destravar==itm2.id) {
                                    itm2.comando_travado = false
                                    println("Destravou " + itm2.nome)
                                }
                            }
                        }
                        println(itm1.resultado_positivo)
                        itm1.resolvido = true
                        if(itm1.cena_alvo > -1) {
                            this.cena_atual = itm1.cena_alvo
                            printDescricaoCena()
                        }
                    }
                }
                else {
                    println(itm1.resultado_negativo==null ? "Comando inválido" : itm1.resultado_negativo )
                }
                achou = true
            }
        }
        if(achou == false)
        {
            println("Item não encontrado")
        }
    }
    def combinarItem(def item1, def item2) {
        boolean achou = false;
        this.inventario.itens.each{ inv ->
            //Checar se tem o item(use) no inventário
            if(inv.nome == item1) {
                this.cenas[this.cena_atual].itens.each{ itm ->
                    //Verifica se o item(with) existe na cena
                    if(itm.nome == item2) {
                         if(itm.comando_correto == ("use " + inv.nome + " with " + itm.nome)) {
                            println(itm.resultado_positivo)
                            itm.resolvido = -1 
                            if(itm.cena_alvo > -1) {
                                this.cena_atual = itm.cena_alvo
                                printDescricaoCena()
                            }
                            else {
                                println("Erro")
                            }
                        } 
                        achou = true
                    }
                }
                if(achou == false) {
                    println("Item não encontrado")
                }
            }
            else {
                println(inv.resultado_negativo==null ? "Comando inválido" : inv.resultado_negativo )
            }
        }
    }
}

class Inventario {
    static indice = 0
    //Vetor de objetos
    List<Objeto> itens

}
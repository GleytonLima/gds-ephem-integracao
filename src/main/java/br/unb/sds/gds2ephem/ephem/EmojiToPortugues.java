package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class EmojiToPortugues {

    private static final Map<String, String> emojiParaTexto = new HashMap<>();

    static {
        // ROSTOS SORRIDENTES
        emojiParaTexto.put("😀", "[rosto sorridente]");
        emojiParaTexto.put("😃", "[rosto sorridente com olhos grandes]");
        emojiParaTexto.put("😄", "[rosto sorridente com olhos sorridentes]");
        emojiParaTexto.put("😁", "[rosto radiante com olhos sorridentes]");
        emojiParaTexto.put("😆", "[rosto sorridente com olhos fechados]");
        emojiParaTexto.put("😅", "[rosto sorridente com suor]");
        emojiParaTexto.put("🤣", "[rolando de rir]");
        emojiParaTexto.put("😂", "[rosto com lágrimas de alegria]");
        emojiParaTexto.put("🙂", "[rosto levemente sorridente]");
        emojiParaTexto.put("🙃", "[rosto de cabeça para baixo]");
        emojiParaTexto.put("😉", "[rosto piscando]");
        emojiParaTexto.put("😊", "[rosto sorridente com olhos sorridentes]");
        emojiParaTexto.put("😇", "[rosto sorridente com auréola]");

        // ROSTOS AFETUOSOS
        emojiParaTexto.put("🥰", "[rosto sorridente com corações]");
        emojiParaTexto.put("😍", "[rosto sorridente com olhos de coração]");
        emojiParaTexto.put("🤩", "[rosto estrelado]");
        emojiParaTexto.put("😘", "[rosto mandando beijo]");
        emojiParaTexto.put("😗", "[rosto beijando]");
        emojiParaTexto.put("☺️", "[rosto sorridente]");
        emojiParaTexto.put("😚", "[rosto beijando com olhos fechados]");
        emojiParaTexto.put("😙", "[rosto beijando com olhos sorridentes]");
        emojiParaTexto.put("🥲", "[rosto sorridente com lágrima]");

        // ROSTOS COM LÍNGUA
        emojiParaTexto.put("😋", "[rosto saboreando comida]");
        emojiParaTexto.put("😛", "[rosto com língua]");
        emojiParaTexto.put("😜", "[rosto piscando com língua]");
        emojiParaTexto.put("🤪", "[rosto maluco]");
        emojiParaTexto.put("😝", "[rosto com língua e olhos fechados]");
        emojiParaTexto.put("🤑", "[rosto com símbolo de dinheiro]");

        // ROSTOS COM MÃOS
        emojiParaTexto.put("🤗", "[rosto abraçando]");
        emojiParaTexto.put("🤭", "[rosto com mão sobre a boca]");
        emojiParaTexto.put("🤫", "[rosto pedindo silêncio]");
        emojiParaTexto.put("🤔", "[rosto pensativo]");
        emojiParaTexto.put("🤐", "[rosto de boca fechada]");
        emojiParaTexto.put("🤨", "[rosto com sobrancelha levantada]");

        // ROSTOS NEUTROS/CÉTICOS
        emojiParaTexto.put("😐", "[rosto neutro]");
        emojiParaTexto.put("😑", "[rosto inexpressivo]");
        emojiParaTexto.put("😶", "[rosto sem boca]");
        emojiParaTexto.put("😶‍🌫️", "[rosto nas nuvens]");
        emojiParaTexto.put("😏", "[rosto sorrindo ironicamente]");
        emojiParaTexto.put("😒", "[rosto descontente]");
        emojiParaTexto.put("🙄", "[rosto com olhos revirados]");
        emojiParaTexto.put("😬", "[rosto fazendo careta]");
        emojiParaTexto.put("😮‍💨", "[rosto exalando]");
        emojiParaTexto.put("🤥", "[rosto mentiroso]");

        // ROSTOS SONOLENTOS/DOENTES
        emojiParaTexto.put("😌", "[rosto aliviado]");
        emojiParaTexto.put("😔", "[rosto pensativo]");
        emojiParaTexto.put("😪", "[rosto com sono]");
        emojiParaTexto.put("🤤", "[rosto babando]");
        emojiParaTexto.put("😴", "[rosto dormindo]");
        emojiParaTexto.put("😷", "[rosto com máscara médica]");
        emojiParaTexto.put("🤒", "[rosto com termômetro]");
        emojiParaTexto.put("🤕", "[rosto com bandagem]");
        emojiParaTexto.put("🤢", "[rosto enjoado]");
        emojiParaTexto.put("🤮", "[rosto vomitando]");
        emojiParaTexto.put("🤧", "[rosto espirrando]");
        emojiParaTexto.put("🥵", "[rosto vermelho com calor]");
        emojiParaTexto.put("🥶", "[rosto azul de frio]");
        emojiParaTexto.put("🥴", "[rosto embriagado]");
        emojiParaTexto.put("😵", "[rosto tonto]");
        emojiParaTexto.put("😵‍💫", "[rosto com espirais]");
        emojiParaTexto.put("🤯", "[cabeça explodindo]");

        // ROSTOS NEGATIVOS
        emojiParaTexto.put("😕", "[rosto confuso]");
        emojiParaTexto.put("😟", "[rosto preocupado]");
        emojiParaTexto.put("🙁", "[rosto levemente franzido]");
        emojiParaTexto.put("☹️", "[rosto franzido]");
        emojiParaTexto.put("😮", "[rosto com boca aberta]");
        emojiParaTexto.put("😯", "[rosto surpreso]");
        emojiParaTexto.put("😲", "[rosto atônito]");
        emojiParaTexto.put("😳", "[rosto ruborizado]");
        emojiParaTexto.put("🥺", "[rosto suplicante]");
        emojiParaTexto.put("😦", "[rosto franzido com boca aberta]");
        emojiParaTexto.put("😧", "[rosto angustiado]");
        emojiParaTexto.put("😨", "[rosto amedrontado]");
        emojiParaTexto.put("😰", "[rosto ansioso com suor]");
        emojiParaTexto.put("😥", "[rosto triste mas aliviado]");
        emojiParaTexto.put("😢", "[rosto chorando]");
        emojiParaTexto.put("😭", "[rosto chorando alto]");
        emojiParaTexto.put("😱", "[rosto gritando de medo]");
        emojiParaTexto.put("😖", "[rosto confuso]");
        emojiParaTexto.put("😣", "[rosto perseverante]");
        emojiParaTexto.put("😞", "[rosto desapontado]");
        emojiParaTexto.put("😓", "[rosto abatido com suor]");
        emojiParaTexto.put("😩", "[rosto exausto]");
        emojiParaTexto.put("😫", "[rosto cansado]");
        emojiParaTexto.put("🥱", "[rosto bocejando]");

        // ROSTOS COM SÍMBOLOS
        emojiParaTexto.put("😤", "[rosto com vapor do nariz]");
        emojiParaTexto.put("😡", "[rosto irritado]");
        emojiParaTexto.put("😠", "[rosto bravo]");
        emojiParaTexto.put("🤬", "[rosto com símbolos na boca]");
        emojiParaTexto.put("😈", "[rosto sorridente com chifres]");
        emojiParaTexto.put("👿", "[rosto zangado com chifres]");
        emojiParaTexto.put("💀", "[caveira]");
        emojiParaTexto.put("☠️", "[caveira e ossos cruzados]");
        emojiParaTexto.put("💩", "[cocô]");
        emojiParaTexto.put("🤡", "[rosto de palhaço]");
        emojiParaTexto.put("👹", "[ogro]");
        emojiParaTexto.put("👺", "[duende]");
        emojiParaTexto.put("👻", "[fantasma]");
        emojiParaTexto.put("👽", "[alienígena]");
        emojiParaTexto.put("👾", "[monstro alienígena]");
        emojiParaTexto.put("🤖", "[rosto de robô]");

        // GATOS
        emojiParaTexto.put("😺", "[gato sorridente]");
        emojiParaTexto.put("😸", "[gato sorridente com olhos sorridentes]");
        emojiParaTexto.put("😹", "[gato com lágrimas de alegria]");
        emojiParaTexto.put("😻", "[gato sorridente com olhos de coração]");
        emojiParaTexto.put("😼", "[gato com sorriso irônico]");
        emojiParaTexto.put("😽", "[gato beijando]");
        emojiParaTexto.put("🙀", "[gato cansado]");
        emojiParaTexto.put("😿", "[gato chorando]");
        emojiParaTexto.put("😾", "[gato emburrado]");

        // MACACOS
        emojiParaTexto.put("🙈", "[macaco não vê]");
        emojiParaTexto.put("🙉", "[macaco não ouve]");
        emojiParaTexto.put("🙊", "[macaco não fala]");

        // EMOÇÕES
        emojiParaTexto.put("💋", "[marca de beijo]");
        emojiParaTexto.put("💌", "[carta de amor]");
        emojiParaTexto.put("💘", "[coração com flecha]");
        emojiParaTexto.put("💝", "[coração com fita]");
        emojiParaTexto.put("💖", "[coração brilhante]");
        emojiParaTexto.put("💗", "[coração crescente]");
        emojiParaTexto.put("💓", "[coração batendo]");
        emojiParaTexto.put("💞", "[coração girando]");
        emojiParaTexto.put("💕", "[dois corações]");
        emojiParaTexto.put("💟", "[decoração de coração]");
        emojiParaTexto.put("❣️", "[exclamação de coração]");
        emojiParaTexto.put("💔", "[coração partido]");
        emojiParaTexto.put("❤️‍🔥", "[coração em chamas]");
        emojiParaTexto.put("❤️‍🩹", "[coração curando]");
        emojiParaTexto.put("❤️", "[coração vermelho]");
        emojiParaTexto.put("🧡", "[coração laranja]");
        emojiParaTexto.put("💛", "[coração amarelo]");
        emojiParaTexto.put("💚", "[coração verde]");
        emojiParaTexto.put("💙", "[coração azul]");
        emojiParaTexto.put("💜", "[coração roxo]");
        emojiParaTexto.put("🤎", "[coração marrom]");
        emojiParaTexto.put("🖤", "[coração preto]");
        emojiParaTexto.put("🤍", "[coração branco]");
        emojiParaTexto.put("💯", "[pontuação 100]");
        emojiParaTexto.put("💢", "[símbolo de raiva]");
        emojiParaTexto.put("💥", "[colisão]");
        emojiParaTexto.put("💫", "[tonto]");
        emojiParaTexto.put("💦", "[gotas de suor]");
        emojiParaTexto.put("💨", "[correndo rápido]");
        emojiParaTexto.put("🕳️", "[buraco]");
        emojiParaTexto.put("💣", "[bomba]");
        emojiParaTexto.put("💬", "[balão de fala]");
        emojiParaTexto.put("👁️‍🗨️", "[olho em balão de fala]");
        emojiParaTexto.put("🗨️", "[balão de fala à esquerda]");
        emojiParaTexto.put("🗯️", "[balão de raiva à direita]");
        emojiParaTexto.put("💭", "[balão de pensamento]");
        emojiParaTexto.put("💤", "[dormindo]");

        // MÃOS E PARTES DO CORPO
        emojiParaTexto.put("👋", "[mão acenando]");
        emojiParaTexto.put("🤚", "[mão levantada]");
        emojiParaTexto.put("🖐️", "[mão com dedos separados]");
        emojiParaTexto.put("✋", "[mão levantada]");
        emojiParaTexto.put("🖖", "[saudação vulcana]");
        emojiParaTexto.put("👌", "[gesto de ok]");
        emojiParaTexto.put("🤌", "[dedos juntos]");
        emojiParaTexto.put("🤏", "[mão beliscando]");
        emojiParaTexto.put("✌️", "[mão com sinal de vitória]");
        emojiParaTexto.put("🤞", "[dedos cruzados]");
        emojiParaTexto.put("🤟", "[gesto te amo]");
        emojiParaTexto.put("🤘", "[sinal de chifres]");
        emojiParaTexto.put("🤙", "[chame-me]");
        emojiParaTexto.put("👈", "[apontando para a esquerda]");
        emojiParaTexto.put("👉", "[apontando para a direita]");
        emojiParaTexto.put("👆", "[apontando para cima]");
        emojiParaTexto.put("🖕", "[dedo do meio]");
        emojiParaTexto.put("👇", "[apontando para baixo]");
        emojiParaTexto.put("☝️", "[dedo indicador para cima]");
        emojiParaTexto.put("👍", "[joinha]");
        emojiParaTexto.put("👎", "[negativo]");
        emojiParaTexto.put("✊", "[punho levantado]");
        emojiParaTexto.put("👊", "[soco]");
        emojiParaTexto.put("🤛", "[punho para a esquerda]");
        emojiParaTexto.put("🤜", "[punho para a direita]");
        emojiParaTexto.put("👏", "[palmas]");
        emojiParaTexto.put("🙌", "[mãos para cima]");
        emojiParaTexto.put("👐", "[mãos abertas]");
        emojiParaTexto.put("🤲", "[palmas juntas para cima]");
        emojiParaTexto.put("🤝", "[aperto de mãos]");
        emojiParaTexto.put("🙏", "[mãos em prece]");
        emojiParaTexto.put("✍️", "[mão escrevendo]");
        emojiParaTexto.put("💅", "[esmalte]");
        emojiParaTexto.put("🤳", "[selfie]");
        emojiParaTexto.put("💪", "[bíceps]");
        emojiParaTexto.put("🦾", "[braço mecânico]");
        emojiParaTexto.put("🦿", "[perna mecânica]");
        emojiParaTexto.put("🦵", "[perna]");
        emojiParaTexto.put("🦶", "[pé]");
        emojiParaTexto.put("👂", "[orelha]");
        emojiParaTexto.put("🦻", "[orelha com aparelho auditivo]");
        emojiParaTexto.put("👃", "[nariz]");
        emojiParaTexto.put("🧠", "[cérebro]");
        emojiParaTexto.put("🫀", "[coração anatômico]");
        emojiParaTexto.put("🫁", "[pulmões]");
        emojiParaTexto.put("🦷", "[dente]");
        emojiParaTexto.put("🦴", "[osso]");
        emojiParaTexto.put("👀", "[olhos]");
        emojiParaTexto.put("👁️", "[olho]");
        emojiParaTexto.put("👅", "[língua]");
        emojiParaTexto.put("👄", "[boca]");

        // PESSOAS
        emojiParaTexto.put("👶", "[bebê]");
        emojiParaTexto.put("🧒", "[criança]");
        emojiParaTexto.put("👦", "[menino]");
        emojiParaTexto.put("👧", "[menina]");
        emojiParaTexto.put("🧑", "[pessoa]");
        emojiParaTexto.put("👱", "[pessoa loira]");
        emojiParaTexto.put("👨", "[homem]");
        emojiParaTexto.put("🧔", "[pessoa com barba]");
        emojiParaTexto.put("👩", "[mulher]");
        emojiParaTexto.put("🧓", "[pessoa idosa]");
        emojiParaTexto.put("👴", "[homem idoso]");
        emojiParaTexto.put("👵", "[mulher idosa]");

        // PROFISSÕES E PAPÉIS
        emojiParaTexto.put("👮", "[policial]");
        emojiParaTexto.put("🕵️", "[detetive]");
        emojiParaTexto.put("💂", "[guarda]");
        emojiParaTexto.put("🥷", "[ninja]");
        emojiParaTexto.put("👷", "[trabalhador de construção]");
        emojiParaTexto.put("🤴", "[príncipe]");
        emojiParaTexto.put("👸", "[princesa]");
        emojiParaTexto.put("👳", "[pessoa com turbante]");
        emojiParaTexto.put("👲", "[homem com gorro chinês]");
        emojiParaTexto.put("🧕", "[mulher com véu]");
        emojiParaTexto.put("🤵", "[pessoa de smoking]");
        emojiParaTexto.put("👰", "[pessoa com véu de noiva]");
        emojiParaTexto.put("🤰", "[pessoa grávida]");
        emojiParaTexto.put("🤱", "[amamentando]");

        // GESTOS DE PESSOAS
        emojiParaTexto.put("👼", "[bebê anjo]");
        emojiParaTexto.put("🎅", "[papai noel]");
        emojiParaTexto.put("🤶", "[mamãe noel]");
        emojiParaTexto.put("🦸", "[super-herói]");
        emojiParaTexto.put("🦹", "[super-vilão]");
        emojiParaTexto.put("🧙", "[mago]");
        emojiParaTexto.put("🧚", "[fada]");
        emojiParaTexto.put("🧛", "[vampiro]");
        emojiParaTexto.put("🧜", "[tritão/sereia]");
        emojiParaTexto.put("🧝", "[elfo]");
        emojiParaTexto.put("🧞", "[gênio]");
        emojiParaTexto.put("🧟", "[zumbi]");
        emojiParaTexto.put("💆", "[massagem na cabeça]");
        emojiParaTexto.put("💇", "[corte de cabelo]");
        emojiParaTexto.put("🚶", "[pessoa andando]");
        emojiParaTexto.put("🧍", "[pessoa em pé]");
        emojiParaTexto.put("🧎", "[pessoa ajoelhada]");
        emojiParaTexto.put("🏃", "[pessoa correndo]");
        emojiParaTexto.put("💃", "[mulher dançando]");
        emojiParaTexto.put("🕺", "[homem dançando]");

        // ANIMAIS
        emojiParaTexto.put("🐵", "[cara de macaco]");
        emojiParaTexto.put("🐒", "[macaco]");
        emojiParaTexto.put("🦍", "[gorila]");
        emojiParaTexto.put("🦧", "[orangotango]");
        emojiParaTexto.put("🐶", "[cara de cachorro]");
        emojiParaTexto.put("🐕", "[cachorro]");
        emojiParaTexto.put("🦮", "[cão-guia]");
        emojiParaTexto.put("🐩", "[poodle]");
        emojiParaTexto.put("🐺", "[lobo]");
        emojiParaTexto.put("🦊", "[raposa]");
        emojiParaTexto.put("🦝", "[guaxinim]");
        emojiParaTexto.put("🐱", "[cara de gato]");
        emojiParaTexto.put("🐈", "[gato]");
        emojiParaTexto.put("🦁", "[leão]");
        emojiParaTexto.put("🐯", "[cara de tigre]");
        emojiParaTexto.put("🐅", "[tigre]");
        emojiParaTexto.put("🐆", "[leopardo]");
        emojiParaTexto.put("🐴", "[cara de cavalo]");
        emojiParaTexto.put("🐎", "[cavalo]");
        emojiParaTexto.put("🦄", "[unicórnio]");
        emojiParaTexto.put("🦓", "[zebra]");
        emojiParaTexto.put("🦌", "[cervo]");
        emojiParaTexto.put("🐮", "[cara de vaca]");
        emojiParaTexto.put("🐂", "[boi]");
        emojiParaTexto.put("🐃", "[búfalo d'água]");
        emojiParaTexto.put("🐄", "[vaca]");
        emojiParaTexto.put("🐷", "[cara de porco]");
        emojiParaTexto.put("🐖", "[porco]");
        emojiParaTexto.put("🐗", "[javali]");
        emojiParaTexto.put("🐽", "[focinho de porco]");
        emojiParaTexto.put("🐏", "[carneiro]");
        emojiParaTexto.put("🐑", "[ovelha]");
        emojiParaTexto.put("🐐", "[cabra]");
        emojiParaTexto.put("🐪", "[camelo]");
        emojiParaTexto.put("🐫", "[camelo de duas corcovas]");
        emojiParaTexto.put("🦙", "[lhama]");
        emojiParaTexto.put("🦒", "[girafa]");
        emojiParaTexto.put("🐘", "[elefante]");
        emojiParaTexto.put("🦏", "[rinoceronte]");
        emojiParaTexto.put("🦛", "[hipopótamo]");
        emojiParaTexto.put("🐭", "[cara de rato]");
        emojiParaTexto.put("🐁", "[rato]");
        emojiParaTexto.put("🐀", "[ratazana]");
        emojiParaTexto.put("🐹", "[hamster]");
        emojiParaTexto.put("🐰", "[cara de coelho]");
        emojiParaTexto.put("🐇", "[coelho]");
        emojiParaTexto.put("🐿️", "[esquilo]");
        emojiParaTexto.put("🦔", "[ouriço]");
        emojiParaTexto.put("🦇", "[morcego]");
        emojiParaTexto.put("🐻", "[urso]");
        emojiParaTexto.put("🐨", "[coala]");
        emojiParaTexto.put("🐼", "[panda]");
        emojiParaTexto.put("🦥", "[bicho-preguiça]");
        emojiParaTexto.put("🦦", "[lontra]");
        emojiParaTexto.put("🦨", "[gambá]");
        emojiParaTexto.put("🦘", "[canguru]");
        emojiParaTexto.put("🦡", "[texugo]");

        // AVES
        emojiParaTexto.put("🦃", "[peru]");
        emojiParaTexto.put("🐔", "[galinha]");
        emojiParaTexto.put("🐓", "[galo]");
        emojiParaTexto.put("🐣", "[pintinho nascendo]");
        emojiParaTexto.put("🐤", "[pintinho]");
        emojiParaTexto.put("🐥", "[pintinho de frente]");
        emojiParaTexto.put("🐦", "[pássaro]");
        emojiParaTexto.put("🐧", "[pinguim]");
        emojiParaTexto.put("🕊️", "[pomba]");
        emojiParaTexto.put("🦅", "[águia]");
        emojiParaTexto.put("🦆", "[pato]");
        emojiParaTexto.put("🦢", "[cisne]");
        emojiParaTexto.put("🦉", "[coruja]");
        emojiParaTexto.put("🦤", "[dodô]");
        emojiParaTexto.put("🪶", "[pena]");
        emojiParaTexto.put("🦩", "[flamingo]");
        emojiParaTexto.put("🦚", "[pavão]");
        emojiParaTexto.put("🦜", "[papagaio]");

        // RÉPTEIS E ANFÍBIOS
        emojiParaTexto.put("🐸", "[sapo]");
        emojiParaTexto.put("🐊", "[crocodilo]");
        emojiParaTexto.put("🐢", "[tartaruga]");
        emojiParaTexto.put("🦎", "[lagarto]");
        emojiParaTexto.put("🐍", "[cobra]");
        emojiParaTexto.put("🐲", "[cara de dragão]");
        emojiParaTexto.put("🐉", "[dragão]");
        emojiParaTexto.put("🦕", "[saurópode]");
        emojiParaTexto.put("🦖", "[tiranossauro rex]");

        // CRIATURAS MARINHAS
        emojiParaTexto.put("🐳", "[baleia espirrando]");
        emojiParaTexto.put("🐋", "[baleia]");
        emojiParaTexto.put("🐬", "[golfinho]");
        emojiParaTexto.put("🦭", "[foca]");
        emojiParaTexto.put("🐟", "[peixe]");
        emojiParaTexto.put("🐠", "[peixe tropical]");
        emojiParaTexto.put("🐡", "[baiacu]");
        emojiParaTexto.put("🦈", "[tubarão]");
        emojiParaTexto.put("🐙", "[polvo]");
        emojiParaTexto.put("🐚", "[concha espiral]");
        emojiParaTexto.put("🐌", "[caracol]");
        emojiParaTexto.put("🦋", "[borboleta]");
        emojiParaTexto.put("🐛", "[inseto]");
        emojiParaTexto.put("🐜", "[formiga]");
        emojiParaTexto.put("🐝", "[abelha]");
        emojiParaTexto.put("🪲", "[besouro]");
        emojiParaTexto.put("🐞", "[joaninha]");
        emojiParaTexto.put("🦗", "[grilo]");
        emojiParaTexto.put("🪳", "[barata]");
        emojiParaTexto.put("🕷️", "[aranha]");
        emojiParaTexto.put("🕸️", "[teia de aranha]");
        emojiParaTexto.put("🦂", "[escorpião]");
        emojiParaTexto.put("🦟", "[mosquito]");
        emojiParaTexto.put("🪰", "[mosca]");
        emojiParaTexto.put("🪱", "[verme]");
        emojiParaTexto.put("🦠", "[micróbio]");

        // PLANTAS E FLORES
        emojiParaTexto.put("💐", "[buquê]");
        emojiParaTexto.put("🌸", "[flor de cerejeira]");
        emojiParaTexto.put("💮", "[flor branca]");
        emojiParaTexto.put("🏵️", "[roseta]");
        emojiParaTexto.put("🌹", "[rosa]");
        emojiParaTexto.put("🥀", "[flor murcha]");
        emojiParaTexto.put("🌺", "[hibisco]");
        emojiParaTexto.put("🌻", "[girassol]");
        emojiParaTexto.put("🌼", "[flor]");
        emojiParaTexto.put("🌷", "[tulipa]");
        emojiParaTexto.put("🌱", "[muda]");
        emojiParaTexto.put("🪴", "[planta em vaso]");
        emojiParaTexto.put("🌲", "[árvore perene]");
        emojiParaTexto.put("🌳", "[árvore caducifólia]");
        emojiParaTexto.put("🌴", "[palmeira]");
        emojiParaTexto.put("🌵", "[cacto]");
        emojiParaTexto.put("🌾", "[arroz]");
        emojiParaTexto.put("🌿", "[erva]");
        emojiParaTexto.put("☘️", "[trevo]");
        emojiParaTexto.put("🍀", "[trevo de quatro folhas]");
        emojiParaTexto.put("🍁", "[folha de bordo]");
        emojiParaTexto.put("🍂", "[folha caída]");
        emojiParaTexto.put("🍃", "[folha ao vento]");

        // COMIDAS E BEBIDAS
        emojiParaTexto.put("🍇", "[uvas]");
        emojiParaTexto.put("🍈", "[melão]");
        emojiParaTexto.put("🍉", "[melancia]");
        emojiParaTexto.put("🍊", "[tangerina]");
        emojiParaTexto.put("🍋", "[limão]");
        emojiParaTexto.put("🍌", "[banana]");
        emojiParaTexto.put("🍍", "[abacaxi]");
        emojiParaTexto.put("🥭", "[manga]");
        emojiParaTexto.put("🍎", "[maçã vermelha]");
        emojiParaTexto.put("🍏", "[maçã verde]");
        emojiParaTexto.put("🍐", "[pêra]");
        emojiParaTexto.put("🍑", "[pêssego]");
        emojiParaTexto.put("🍒", "[cerejas]");
        emojiParaTexto.put("🍓", "[morango]");
        emojiParaTexto.put("🫐", "[mirtilos]");
        emojiParaTexto.put("🥝", "[kiwi]");
        emojiParaTexto.put("🍅", "[tomate]");
        emojiParaTexto.put("🫒", "[azeitona]");
        emojiParaTexto.put("🥥", "[coco]");
        emojiParaTexto.put("🥑", "[abacate]");
        emojiParaTexto.put("🍆", "[berinjela]");
        emojiParaTexto.put("🥔", "[batata]");
        emojiParaTexto.put("🥕", "[cenoura]");
        emojiParaTexto.put("🌽", "[espiga de milho]");
        emojiParaTexto.put("🌶️", "[pimenta]");
        emojiParaTexto.put("🫑", "[pimentão]");
        emojiParaTexto.put("🥒", "[pepino]");
        emojiParaTexto.put("🥬", "[folha verde]");
        emojiParaTexto.put("🥦", "[brócolis]");
        emojiParaTexto.put("🧄", "[alho]");
        emojiParaTexto.put("🧅", "[cebola]");
        emojiParaTexto.put("🍄", "[cogumelo]");
        emojiParaTexto.put("🥜", "[amendoim]");
        emojiParaTexto.put("🌰", "[castanha]");
        emojiParaTexto.put("🍞", "[pão]");
        emojiParaTexto.put("🥐", "[croissant]");
        emojiParaTexto.put("🥖", "[baguete]");
        emojiParaTexto.put("🫓", "[pão sírio]");
        emojiParaTexto.put("🥨", "[pretzel]");
        emojiParaTexto.put("🥯", "[bagel]");
        emojiParaTexto.put("🥞", "[panquecas]");
        emojiParaTexto.put("🧇", "[waffle]");
        emojiParaTexto.put("🧀", "[pedaço de queijo]");
        emojiParaTexto.put("🍖", "[osso com carne]");
        emojiParaTexto.put("🍗", "[coxa de frango]");
        emojiParaTexto.put("🥩", "[corte de carne]");
        emojiParaTexto.put("🥓", "[bacon]");
        emojiParaTexto.put("🍔", "[hambúrguer]");
        emojiParaTexto.put("🍟", "[batata frita]");
        emojiParaTexto.put("🍕", "[pizza]");
        emojiParaTexto.put("🌭", "[cachorro-quente]");
        emojiParaTexto.put("🥪", "[sanduíche]");
        emojiParaTexto.put("🌮", "[taco]");
        emojiParaTexto.put("🌯", "[burrito]");
        emojiParaTexto.put("🫔", "[tamale]");
        emojiParaTexto.put("🥙", "[pão pita recheado]");
        emojiParaTexto.put("🧆", "[falafel]");
        emojiParaTexto.put("🥚", "[ovo]");
        emojiParaTexto.put("🍳", "[cozinhando]");
        emojiParaTexto.put("🥘", "[paella]");
        emojiParaTexto.put("🍲", "[ensopado]");
        emojiParaTexto.put("🫕", "[fondue]");
        emojiParaTexto.put("🥣", "[tigela com colher]");
        emojiParaTexto.put("🥗", "[salada verde]");
        emojiParaTexto.put("🍿", "[pipoca]");
        emojiParaTexto.put("🧈", "[manteiga]");
        emojiParaTexto.put("🧂", "[sal]");
        emojiParaTexto.put("🥫", "[comida enlatada]");
        emojiParaTexto.put("🍱", "[marmita]");
        emojiParaTexto.put("🍘", "[biscoito de arroz]");
        emojiParaTexto.put("🍙", "[bolinho de arroz]");
        emojiParaTexto.put("🍚", "[arroz cozido]");
        emojiParaTexto.put("🍛", "[curry com arroz]");
        emojiParaTexto.put("🍜", "[tigela fumegante]");
        emojiParaTexto.put("🍝", "[espaguete]");
        emojiParaTexto.put("🍠", "[batata-doce assada]");
        emojiParaTexto.put("🍢", "[espetinho]");
        emojiParaTexto.put("🍣", "[sushi]");
        emojiParaTexto.put("🍤", "[camarão frito]");
        emojiParaTexto.put("🍥", "[bolinho de peixe]");
        emojiParaTexto.put("🥮", "[bolo da lua]");
        emojiParaTexto.put("🍡", "[dango]");
        emojiParaTexto.put("🥟", "[bolinho]");
        emojiParaTexto.put("🥠", "[biscoito da sorte]");
        emojiParaTexto.put("🥡", "[caixa para viagem]");

        // DOCES E BEBIDAS
        emojiParaTexto.put("🍦", "[sorvete italiano]");
        emojiParaTexto.put("🍧", "[raspadinha]");
        emojiParaTexto.put("🍨", "[sorvete]");
        emojiParaTexto.put("🍩", "[rosquinha]");
        emojiParaTexto.put("🍪", "[biscoito]");
        emojiParaTexto.put("🎂", "[bolo de aniversário]");
        emojiParaTexto.put("🍰", "[fatia de bolo]");
        emojiParaTexto.put("🧁", "[cupcake]");
        emojiParaTexto.put("🥧", "[torta]");
        emojiParaTexto.put("🍫", "[barra de chocolate]");
        emojiParaTexto.put("🍬", "[bala]");
        emojiParaTexto.put("🍭", "[pirulito]");
        emojiParaTexto.put("🍮", "[pudim]");
        emojiParaTexto.put("🍯", "[pote de mel]");
        emojiParaTexto.put("🍼", "[mamadeira]");
        emojiParaTexto.put("🥛", "[copo de leite]");
        emojiParaTexto.put("☕", "[bebida quente]");
        emojiParaTexto.put("🫖", "[bule]");
        emojiParaTexto.put("🍵", "[chá sem alça]");
        emojiParaTexto.put("🍶", "[sake]");
        emojiParaTexto.put("🍾", "[garrafa com rolha]");
        emojiParaTexto.put("🍷", "[taça de vinho]");
        emojiParaTexto.put("🍸", "[coqueteleira]");
        emojiParaTexto.put("🍹", "[bebida tropical]");
        emojiParaTexto.put("🍺", "[caneca de cerveja]");
        emojiParaTexto.put("🍻", "[canecas brindando]");
        emojiParaTexto.put("🥂", "[taças brindando]");
        emojiParaTexto.put("🥃", "[copo baixo]");
        emojiParaTexto.put("🥤", "[copo com canudo]");
        emojiParaTexto.put("🧋", "[chá com bolinhas]");
        emojiParaTexto.put("🧃", "[caixinha de bebida]");
        emojiParaTexto.put("🧉", "[mate]");
        emojiParaTexto.put("🧊", "[cubo de gelo]");

        // OBJETOS E LOCAIS
        emojiParaTexto.put("🚗", "[carro]");
        emojiParaTexto.put("🚕", "[táxi]");
        emojiParaTexto.put("🚙", "[SUV]");
        emojiParaTexto.put("🚌", "[ônibus]");
        emojiParaTexto.put("🚎", "[trólebus]");
        emojiParaTexto.put("🏎️", "[carro de corrida]");
        emojiParaTexto.put("🚓", "[carro de polícia]");
        emojiParaTexto.put("🚑", "[ambulância]");
        emojiParaTexto.put("🚒", "[caminhão de bombeiros]");
        emojiParaTexto.put("🚐", "[van]");
        emojiParaTexto.put("🚚", "[caminhão de entrega]");
        emojiParaTexto.put("🚛", "[caminhão articulado]");
        emojiParaTexto.put("🚜", "[trator]");
        emojiParaTexto.put("🏍️", "[motocicleta]");
        emojiParaTexto.put("🛵", "[scooter]");
        emojiParaTexto.put("🚲", "[bicicleta]");
        emojiParaTexto.put("🛴", "[patinete]");
        emojiParaTexto.put("🚂", "[locomotiva]");
        emojiParaTexto.put("🚆", "[trem]");
        emojiParaTexto.put("🚇", "[metrô]");
        emojiParaTexto.put("✈️", "[avião]");
        emojiParaTexto.put("🚀", "[foguete]");
        emojiParaTexto.put("🛸", "[disco voador]");
        emojiParaTexto.put("🚁", "[helicóptero]");
        emojiParaTexto.put("⛵", "[veleiro]");
        emojiParaTexto.put("🚢", "[navio]");

        // PRÉDIOS E LOCAIS
        emojiParaTexto.put("🏠", "[casa]");
        emojiParaTexto.put("🏡", "[casa com jardim]");
        emojiParaTexto.put("🏢", "[prédio de escritórios]");
        emojiParaTexto.put("🏣", "[correio japonês]");
        emojiParaTexto.put("🏤", "[correio]");
        emojiParaTexto.put("🏥", "[hospital]");
        emojiParaTexto.put("🏦", "[banco]");
        emojiParaTexto.put("🏨", "[hotel]");
        emojiParaTexto.put("🏩", "[hotel do amor]");
        emojiParaTexto.put("🏪", "[loja de conveniência]");
        emojiParaTexto.put("🏫", "[escola]");
        emojiParaTexto.put("🏛️", "[edifício clássico]");
        emojiParaTexto.put("⛪", "[igreja]");
        emojiParaTexto.put("🕌", "[mesquita]");
        emojiParaTexto.put("🕍", "[sinagoga]");
        emojiParaTexto.put("🕋", "[kaaba]");
        emojiParaTexto.put("⛲", "[fonte]");
        emojiParaTexto.put("⛺", "[barraca]");
        emojiParaTexto.put("🌁", "[neblina]");
        emojiParaTexto.put("🌃", "[noite estrelada]");
        emojiParaTexto.put("🌄", "[nascer do sol nas montanhas]");
        emojiParaTexto.put("🌅", "[nascer do sol]");
        emojiParaTexto.put("🌆", "[paisagem urbana ao entardecer]");
        emojiParaTexto.put("🌇", "[pôr do sol]");
        emojiParaTexto.put("🌉", "[ponte à noite]");
        emojiParaTexto.put("🎠", "[carrossel]");
        emojiParaTexto.put("🎡", "[roda-gigante]");
        emojiParaTexto.put("🎢", "[montanha-russa]");
        emojiParaTexto.put("🚂", "[locomotiva]");

        // TEMPO E CLIMA
        emojiParaTexto.put("⌛", "[ampulheta]");
        emojiParaTexto.put("⏳", "[ampulheta em execução]");
        emojiParaTexto.put("⌚", "[relógio de pulso]");
        emojiParaTexto.put("⏰", "[despertador]");
        emojiParaTexto.put("⏱️", "[cronômetro]");
        emojiParaTexto.put("⏲️", "[temporizador]");
        emojiParaTexto.put("🕰️", "[relógio de mesa]");
        emojiParaTexto.put("🌡️", "[termômetro]");
        emojiParaTexto.put("☀️", "[sol]");
        emojiParaTexto.put("🌝", "[lua cheia com rosto]");
        emojiParaTexto.put("🌛", "[lua crescente com rosto]");
        emojiParaTexto.put("🌜", "[lua minguante com rosto]");
        emojiParaTexto.put("🌚", "[lua nova com rosto]");
        emojiParaTexto.put("🌑", "[lua nova]");
        emojiParaTexto.put("🌒", "[lua crescente]");
        emojiParaTexto.put("🌓", "[lua no primeiro quarto]");
        emojiParaTexto.put("🌔", "[lua gibosa crescente]");
        emojiParaTexto.put("🌕", "[lua cheia]");
        emojiParaTexto.put("🌖", "[lua gibosa minguante]");
        emojiParaTexto.put("🌗", "[lua no último quarto]");
        emojiParaTexto.put("🌘", "[lua minguante]");
        emojiParaTexto.put("🌙", "[lua crescente]");
        emojiParaTexto.put("🌞", "[sol com rosto]");
        emojiParaTexto.put("⭐", "[estrela]");
        emojiParaTexto.put("🌟", "[estrela brilhante]");
        emojiParaTexto.put("🌠", "[estrela cadente]");
        emojiParaTexto.put("☁️", "[nuvem]");
        emojiParaTexto.put("⛅", "[sol atrás de nuvem]");
        emojiParaTexto.put("⛈️", "[nuvem com relâmpago e chuva]");
        emojiParaTexto.put("🌤️", "[sol atrás de nuvem pequena]");
        emojiParaTexto.put("🌥️", "[sol atrás de nuvem grande]");
        emojiParaTexto.put("🌦️", "[sol atrás de nuvem com chuva]");
        emojiParaTexto.put("🌧️", "[nuvem com chuva]");
        emojiParaTexto.put("🌨️", "[nuvem com neve]");
        emojiParaTexto.put("🌩️", "[nuvem com relâmpago]");
        emojiParaTexto.put("🌪️", "[tornado]");
        emojiParaTexto.put("🌫️", "[névoa]");
        emojiParaTexto.put("🌬️", "[rosto soprando vento]");
        emojiParaTexto.put("🌈", "[arco-íris]");
        emojiParaTexto.put("☂️", "[guarda-chuva]");
        emojiParaTexto.put("☔", "[guarda-chuva com gotas de chuva]");
        emojiParaTexto.put("⚡", "[alta tensão]");
        emojiParaTexto.put("❄️", "[floco de neve]");
        emojiParaTexto.put("☃️", "[boneco de neve]");
        emojiParaTexto.put("⛄", "[boneco de neve sem neve]");
        emojiParaTexto.put("☄️", "[cometa]");
        emojiParaTexto.put("🔥", "[fogo]");
        emojiParaTexto.put("💧", "[gota]");
        emojiParaTexto.put("🌊", "[onda]");

        // ESPORTES
        emojiParaTexto.put("⚽", "[bola de futebol]");
        emojiParaTexto.put("🏀", "[basquete]");
        emojiParaTexto.put("🏈", "[futebol americano]");
        emojiParaTexto.put("⚾", "[beisebol]");
        emojiParaTexto.put("🥎", "[softball]");
        emojiParaTexto.put("🎾", "[tênis]");
        emojiParaTexto.put("🏐", "[vôlei]");
        emojiParaTexto.put("🏉", "[rúgbi]");
        emojiParaTexto.put("🥏", "[disco voador]");
        emojiParaTexto.put("🎱", "[bilhar]");
        emojiParaTexto.put("🪀", "[ioiô]");
        emojiParaTexto.put("🏓", "[tênis de mesa]");
        emojiParaTexto.put("🏸", "[badminton]");
        emojiParaTexto.put("🏒", "[hóquei no gelo]");
        emojiParaTexto.put("🏑", "[hóquei em campo]");
        emojiParaTexto.put("🥍", "[lacrosse]");
        emojiParaTexto.put("🏏", "[críquete]");
        emojiParaTexto.put("🪃", "[bumerangue]");
        emojiParaTexto.put("🥅", "[gol]");
        emojiParaTexto.put("⛳", "[bandeira no buraco]");
        emojiParaTexto.put("🪁", "[pipa]");
        emojiParaTexto.put("🏹", "[arco e flecha]");
        emojiParaTexto.put("🎣", "[vara de pescar]");
        emojiParaTexto.put("🤿", "[máscara de mergulho]");
        emojiParaTexto.put("🥊", "[luva de boxe]");
        emojiParaTexto.put("🥋", "[uniforme de artes marciais]");
        emojiParaTexto.put("🎽", "[camisa de corrida]");
        emojiParaTexto.put("🛹", "[skate]");
        emojiParaTexto.put("🛼", "[patins]");
        emojiParaTexto.put("🛷", "[trenó]");
        emojiParaTexto.put("⛸️", "[patins de gelo]");
        emojiParaTexto.put("🥌", "[pedra de curling]");

        // MÚSICA E ARTE
        emojiParaTexto.put("🎭", "[artes cênicas]");
        emojiParaTexto.put("🎨", "[paleta de pintura]");
        emojiParaTexto.put("🎬", "[claquete]");
        emojiParaTexto.put("🎤", "[microfone]");
        emojiParaTexto.put("🎧", "[fone de ouvido]");
        emojiParaTexto.put("🎼", "[partitura musical]");
        emojiParaTexto.put("🎹", "[piano]");
        emojiParaTexto.put("🥁", "[tambor]");
        emojiParaTexto.put("🎷", "[saxofone]");
        emojiParaTexto.put("🎺", "[trompete]");
        emojiParaTexto.put("🎸", "[guitarra]");
        emojiParaTexto.put("🪕", "[banjo]");
        emojiParaTexto.put("🎻", "[violino]");
        emojiParaTexto.put("🎲", "[dado]");
        emojiParaTexto.put("♟️", "[peça de xadrez]");
        emojiParaTexto.put("🎯", "[acertar o alvo]");
        emojiParaTexto.put("🎮", "[controle de videogame]");
        emojiParaTexto.put("🎰", "[caça-níqueis]");
        emojiParaTexto.put("🧩", "[peça de quebra-cabeça]");

        // TECNOLOGIA E ESCRITÓRIO
        emojiParaTexto.put("📱", "[celular]");
        emojiParaTexto.put("📲", "[celular com seta]");
        emojiParaTexto.put("💻", "[laptop]");
        emojiParaTexto.put("⌨️", "[teclado]");
        emojiParaTexto.put("🖥️", "[computador desktop]");
        emojiParaTexto.put("🖨️", "[impressora]");
        emojiParaTexto.put("🖱️", "[mouse]");
        emojiParaTexto.put("💽", "[disco de computador]");
        emojiParaTexto.put("💾", "[disquete]");
        emojiParaTexto.put("💿", "[disco óptico]");
        emojiParaTexto.put("📀", "[DVD]");
        emojiParaTexto.put("🧮", "[ábaco]");
        emojiParaTexto.put("🎥", "[câmera de cinema]");
        emojiParaTexto.put("🎞️", "[quadros de filme]");
        emojiParaTexto.put("📽️", "[projetor de filmes]");
        emojiParaTexto.put("📺", "[televisão]");
        emojiParaTexto.put("📷", "[câmera]");
        emojiParaTexto.put("📸", "[câmera com flash]");
        emojiParaTexto.put("📹", "[câmera de vídeo]");
        emojiParaTexto.put("📼", "[videocassete]");
        emojiParaTexto.put("🔍", "[lupa inclinada para a esquerda]");
        emojiParaTexto.put("🔎", "[lupa inclinada para a direita]");
        emojiParaTexto.put("🕯️", "[vela]");
        emojiParaTexto.put("💡", "[lâmpada]");
        emojiParaTexto.put("🔦", "[lanterna]");
        emojiParaTexto.put("🏮", "[lanterna de papel vermelha]");
        emojiParaTexto.put("📔", "[caderno com capa decorativa]");
        emojiParaTexto.put("📕", "[livro fechado]");
        emojiParaTexto.put("📖", "[livro aberto]");
        emojiParaTexto.put("📗", "[livro verde]");
        emojiParaTexto.put("📘", "[livro azul]");
        emojiParaTexto.put("📙", "[livro laranja]");
        emojiParaTexto.put("📚", "[livros]");
        emojiParaTexto.put("📓", "[caderno]");
        emojiParaTexto.put("📒", "[livro contábil]");
        emojiParaTexto.put("📃", "[página com dobra]");
        emojiParaTexto.put("📜", "[pergaminho]");
        emojiParaTexto.put("📄", "[página virada para cima]");
        emojiParaTexto.put("📰", "[jornal]");
        emojiParaTexto.put("🗞️", "[jornal enrolado]");
        emojiParaTexto.put("📑", "[marcadores de página]");
        emojiParaTexto.put("🔖", "[marcador]");
        emojiParaTexto.put("🏷️", "[etiqueta]");
        emojiParaTexto.put("✏️", "[lápis]");
        emojiParaTexto.put("✒️", "[caneta-tinteiro]");
        emojiParaTexto.put("🖋️", "[caneta]");
        emojiParaTexto.put("🖊️", "[caneta esferográfica]");
        emojiParaTexto.put("🖌️", "[pincel]");
        emojiParaTexto.put("🖍️", "[giz de cera]");
        emojiParaTexto.put("📝", "[nota]");
        emojiParaTexto.put("💼", "[pasta]");
        emojiParaTexto.put("📁", "[pasta de arquivos]");
        emojiParaTexto.put("📂", "[pasta de arquivos aberta]");
        emojiParaTexto.put("🗂️", "[divisores de cartão]");
        emojiParaTexto.put("📅", "[calendário]");
        emojiParaTexto.put("📆", "[calendário destacável]");
        emojiParaTexto.put("🗒️", "[bloco espiral]");
        emojiParaTexto.put("🗓️", "[calendário espiral]");
        emojiParaTexto.put("📇", "[fichário]");
        emojiParaTexto.put("📈", "[gráfico aumentando]");
        emojiParaTexto.put("📉", "[gráfico diminuindo]");
        emojiParaTexto.put("📊", "[gráfico de barras]");
        emojiParaTexto.put("📋", "[prancheta]");
        emojiParaTexto.put("📌", "[alfinete]");
        emojiParaTexto.put("📍", "[alfinete redondo]");
        emojiParaTexto.put("📎", "[clipe de papel]");
        emojiParaTexto.put("🖇️", "[clipes de papel ligados]");
        emojiParaTexto.put("📏", "[régua reta]");
        emojiParaTexto.put("📐", "[régua triangular]");
        emojiParaTexto.put("✂️", "[tesoura]");
        emojiParaTexto.put("🗃️", "[caixa de arquivo]");
        emojiParaTexto.put("🗄️", "[armário de arquivo]");
        emojiParaTexto.put("🗑️", "[cesto de lixo]");
        emojiParaTexto.put("🔒", "[cadeado]");
        emojiParaTexto.put("🔓", "[cadeado aberto]");
        emojiParaTexto.put("🔏", "[cadeado com caneta]");
        emojiParaTexto.put("🔐", "[cadeado fechado com chave]");
        emojiParaTexto.put("🔑", "[chave]");
        emojiParaTexto.put("🗝️", "[chave antiga]");
        emojiParaTexto.put("🔨", "[martelo]");
        emojiParaTexto.put("🪓", "[machado]");
        emojiParaTexto.put("⛏️", "[picareta]");
        emojiParaTexto.put("⚒️", "[martelo e picareta]");
        emojiParaTexto.put("🛠️", "[martelo e chave inglesa]");
        emojiParaTexto.put("🗡️", "[adaga]");
        emojiParaTexto.put("⚔️", "[espadas cruzadas]");
        emojiParaTexto.put("🔫", "[pistola d'água]");
        emojiParaTexto.put("🪃", "[bumerangue]");
        emojiParaTexto.put("🏹", "[arco e flecha]");
        emojiParaTexto.put("🛡️", "[escudo]");
        emojiParaTexto.put("🔧", "[chave inglesa]");
        emojiParaTexto.put("🔩", "[porca e parafuso]");
        emojiParaTexto.put("⚙️", "[engrenagem]");
        emojiParaTexto.put("🗜️", "[braçadeira]");
        emojiParaTexto.put("⚖️", "[balança]");
        emojiParaTexto.put("🦯", "[bengala]");
        emojiParaTexto.put("🔗", "[link]");
        emojiParaTexto.put("⛓️", "[correntes]");
        emojiParaTexto.put("🧰", "[caixa de ferramentas]");
        emojiParaTexto.put("🧲", "[ímã]");
        emojiParaTexto.put("⚗️", "[alambique]");
        emojiParaTexto.put("🧪", "[tubo de ensaio]");
        emojiParaTexto.put("🧫", "[placa de petri]");
        emojiParaTexto.put("🧬", "[DNA]");
        emojiParaTexto.put("🔬", "[microscópio]");
        emojiParaTexto.put("🔭", "[telescópio]");
        emojiParaTexto.put("📡", "[antena de satélite]");
        emojiParaTexto.put("💉", "[seringa]");
        emojiParaTexto.put("🩸", "[gota de sangue]");
        emojiParaTexto.put("💊", "[pílula]");
        emojiParaTexto.put("🩹", "[bandagem adesiva]");
        emojiParaTexto.put("🩺", "[estetoscópio]");
        emojiParaTexto.put("🚪", "[porta]");
        emojiParaTexto.put("🛗", "[elevador]");
        emojiParaTexto.put("🪞", "[espelho]");
        emojiParaTexto.put("🪟", "[janela]");
        emojiParaTexto.put("🛏️", "[cama]");
        emojiParaTexto.put("🛋️", "[sofá e lâmpada]");
        emojiParaTexto.put("🪑", "[cadeira]");
        emojiParaTexto.put("🚽", "[vaso sanitário]");
        emojiParaTexto.put("🪠", "[desentupidor]");
        emojiParaTexto.put("🚿", "[chuveiro]");
        emojiParaTexto.put("🛁", "[banheira]");
        emojiParaTexto.put("🪤", "[ratoeira]");
        emojiParaTexto.put("🪒", "[navalha]");
        emojiParaTexto.put("🧴", "[frasco de loção]");
        emojiParaTexto.put("🧷", "[alfinete de segurança]");
        emojiParaTexto.put("🧹", "[vassoura]");
        emojiParaTexto.put("🧺", "[cesto]");
        emojiParaTexto.put("🧻", "[rolo de papel]");
        emojiParaTexto.put("🪣", "[balde]");
        emojiParaTexto.put("🧼", "[sabão]");
        emojiParaTexto.put("🪥", "[escova de dentes]");
        emojiParaTexto.put("🧽", "[esponja]");
        emojiParaTexto.put("🧯", "[extintor de incêndio]");
        emojiParaTexto.put("🛒", "[carrinho de compras]");

        // SÍMBOLOS E SINAIS
        emojiParaTexto.put("🚫", "[proibido]");
        emojiParaTexto.put("⛔", "[entrada proibida]");
        emojiParaTexto.put("📛", "[crachá com nome]");
        emojiParaTexto.put("🚷", "[proibida a passagem de pedestres]");
        emojiParaTexto.put("🚯", "[proibido jogar lixo]");
        emojiParaTexto.put("🚳", "[proibido bicicletas]");
        emojiParaTexto.put("🚱", "[água não potável]");
        emojiParaTexto.put("🚭", "[proibido fumar]");
        emojiParaTexto.put("🔞", "[proibido para menores de 18]");
        emojiParaTexto.put("☢️", "[radioativo]");
        emojiParaTexto.put("☣️", "[risco biológico]");
        emojiParaTexto.put("⚠️", "[atenção]");
        emojiParaTexto.put("🚸", "[travessia de crianças]");
        emojiParaTexto.put("⚕️", "[símbolo médico]");
        emojiParaTexto.put("♻️", "[símbolo de reciclagem]");
        emojiParaTexto.put("✅", "[marca de verificação]");
        emojiParaTexto.put("❌", "[marca de x]");
        emojiParaTexto.put("❓", "[ponto de interrogação]");
        emojiParaTexto.put("❗", "[ponto de exclamação]");
        emojiParaTexto.put("💯", "[pontuação 100]");

        // BANDEIRAS
        emojiParaTexto.put("🏁", "[bandeira quadriculada]");
        emojiParaTexto.put("🚩", "[bandeira triangular]");
        emojiParaTexto.put("🎌", "[bandeiras cruzadas]");
        emojiParaTexto.put("🏴", "[bandeira preta]");
        emojiParaTexto.put("🏳️", "[bandeira branca]");
        emojiParaTexto.put("🏳️‍🌈", "[bandeira arco-íris]");
        emojiParaTexto.put("🏳️‍⚧️", "[bandeira transgênero]");
        emojiParaTexto.put("🏴‍☠️", "[bandeira pirata]");
        emojiParaTexto.put("🇧🇷", "[bandeira do Brasil]");
    }

    /**
     * Converte emojis em um texto para suas descrições em português
     *
     * @param texto O texto com emojis
     * @return O texto com emojis substituídos por descrições em português entre colchetes
     */
    public static String converterEmojisParaPortugues(String texto) {
        if (isNull(texto)) {
            return null;
        }

        try {


            String resultado = texto;
            for (Map.Entry<String, String> entrada : emojiParaTexto.entrySet()) {
                resultado = resultado.replace(entrada.getKey(), entrada.getValue());
            }

            // Opcional: identificar outros emojis não mapeados e substituí-los por um valor genérico
            // Isso pode ser feito usando uma regex mais complexa para identificar caracteres no intervalo Unicode de emojis
            String regex = "[\\x{1F000}-\\x{1FFFF}\\x{2600}-\\x{27BF}\\x{FE00}-\\x{FEEE}]";
            resultado = resultado.replaceAll(regex, "[emoji]");

            return resultado;
        } catch (Exception e) {
            log.error("Erro ao converter emojis para texto", e);
            return texto;
        }
    }

    public static void tratarCaracteresEspeciaisEventoIntegracao(EventoIntegracao evento) {
        try {
            // Limpar o emoji do campo data
            if (nonNull(evento.getData())) {
                JsonNode eventoDetalhesNode = evento.getData().get("evento_detalhes");
                if (nonNull(eventoDetalhesNode) && eventoDetalhesNode.isTextual()) {
                    String eventoDetalhes = eventoDetalhesNode.asText();
                    ((ObjectNode) evento.getData()).put("evento_detalhes", converterEmojisParaPortugues(eventoDetalhes));
                }
            }
            // Limpar o emoji do campo aditional_data
            if (nonNull(evento.getAditionalData())) {
                JsonNode detalhesNode = evento.getAditionalData().get("Detalhes");
                if (nonNull(detalhesNode) && detalhesNode.isTextual()) {
                    String detalhes = detalhesNode.asText();
                    ((ObjectNode) evento.getAditionalData()).put("Detalhes", converterEmojisParaPortugues(detalhes));
                }
                JsonNode desejaInformarDetalhesNode = evento.getAditionalData().get("Deseja informar mais detalhes?");
                if (nonNull(desejaInformarDetalhesNode) && desejaInformarDetalhesNode.isTextual()) {
                    String desejaInformarDetalhes = desejaInformarDetalhesNode.asText();
                    ((ObjectNode) evento.getAditionalData()).put("Deseja informar mais detalhes?", converterEmojisParaPortugues(desejaInformarDetalhes));
                }
            }

        } catch (Exception e) {
            log.error("Erro ao tratar caracteres especiais", e);
        }
    }
}
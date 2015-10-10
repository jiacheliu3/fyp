package gro

import org.ansj.app.keyword.KeyWordComputer
import org.ansj.app.keyword.Keyword

class AnsjManager {
	public static Set<String> separate(String str){
		int num=Math.ceil(Math.sqrt(str.length()));
		KeyWordComputer kwc = new KeyWordComputer(num);
		String title="";
		String content=str;
		Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
		println result.class;
		println result;
		
	}
	public static void main(String[] args){
		//KeyWordComputer kwc = new KeyWordComputer(5);
		String title = "ά�����ܷ���˹ŵ�ǽ���ί�������ӻ�";
		String content = "�ж���˹������Ա��9�����罻��վ���ر�ʾ�����������ǰ��Ա˹ŵ�ǣ��Ѿ�����ί�������ıӻ������������ڷ��������Ӻ��漴ɾ��������˹���־ܾ��������ۣ���һֱЭ��˹ŵ�ǵ�ά�����ܷ�������Ͷ��ί����������������˹�����������ίԱ����ϯ��ʲ�Ʒ��ڸ�������������¶˹ŵ���ѽ���ί�������ıӻ����飬�������Ϊ˹ŵ�ǵĶ����������½�չ���������������ڼ�������������ɾ������ʲ�Ʒ�������ǿ�������˹��Ӫ����̨�����Ų�����˵��������̨�Ѿ��������ϣ�����ʲ�Ʒ���������������ݡ�����ί������פĪ˹�ƴ�ʹ�ݡ�����˹��ͳ�������ˡ��Լ��⽻�����ܾ��������ۡ���ά�����ܾͷ���˹ŵ������ʽ����ί�������ıӻ���˵�����ʵ�ʱ�乫���йؾ���������˹ŵ������Ŀǰ����Ī˹��л��÷���ֻ��������������������ڡ�����ǰ��Լ20�������ύ�ӻ����룬ί��������������ϺͲ���ά�ǣ��Ⱥ��ʾ��Ӧ������˹ŵ�ǻ�û������������������һ���⽻�粨������ά����ͳĪ����˹��ר�������ڱ�ŷ�޶���Ի���˹ŵ���ڻ���Ϊ�ɾܾ������¼������¹���֮һ��������ͻȻת�ڷ磬�ⳤ�����]�ű�ʾԸ����κ������Ǹ����ǿ����ʱ����û�йر���ջ���ר�����䡣";
		//Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
		separate(content);
		
	}
}

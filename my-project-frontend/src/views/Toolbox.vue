<template>
  <div class="toolbox-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <h3>分类</h3>
          <el-menu
            :default-active="activeTool"
            @select="onCategorySelect"
            class="category-menu"
            unique-opened
          >
            <el-sub-menu index="外贸">
              <template #title>外贸行业</template>
              <el-menu-item index="外贸:汇率">汇率换算</el-menu-item>
              <el-menu-item index="外贸:运费">国际快递运费估算</el-menu-item>
              <el-menu-item index="外贸:邮件">邮件模板生成器</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="程序员">
              <template #title>程序员</template>
              <el-menu-item index="程序员:json">JSON 格式化</el-menu-item>
              <el-menu-item index="程序员:base64">Base64 编解码</el-menu-item>
              <el-menu-item index="程序员:cron">Cron 表达式生成器</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="学生">
              <template #title>学生</template>
              <el-menu-item index="学生:公式">公式编辑器</el-menu-item>
              <el-menu-item index="学生:计算器">在线计算器</el-menu-item>
              <el-menu-item index="学生:作文">四六级作文批改</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="职场">
              <template #title>职场</template>
              <el-menu-item index="职场:简历">简历生成器</el-menu-item>
              <el-menu-item index="职场:ppt">PPT 模板下载</el-menu-item>
              <el-menu-item index="职场:okr">OKR/KPI 生成器</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card>
          <h2>{{ activeToolTitle }}</h2>

          <!-- 外贸：汇率换算 -->
          <div v-if="activeTool === '外贸:汇率'">
            <el-form :model="rateModel" label-width="100px">
              <el-form-item label="来源货币">
                <el-select v-model="rateModel.from" placeholder="货币">
                  <el-option v-for="c in currencies" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
              <el-form-item label="目标货币">
                <el-select v-model="rateModel.to" placeholder="货币">
                  <el-option v-for="c in currencies" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
              <el-form-item label="汇率（1 from = ? to）">
                <el-input-number v-model="rateModel.rate" :min="0" :step="0.0001" />
              </el-form-item>
              <el-form-item label="金额">
                <el-input-number v-model="rateModel.amount" :min="0" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="convertRate">换算</el-button>
                <el-button @click="rateReset">重置</el-button>
              </el-form-item>
            </el-form>

            <div v-if="rateResult !== null" style="margin-top:12px">
              <el-alert :title="`结果： ${rateModel.amount} ${rateModel.from} = ${rateResult} ${rateModel.to}`" type="success" show-icon />
            </div>
          </div>

          <!-- 外贸：运费估算 -->
          <div v-if="activeTool === '外贸:运费'">
            <el-form :model="shipModel" label-width="120px">
              <el-form-item label="重量 (kg)">
                <el-input-number v-model="shipModel.weight" :min="0" />
              </el-form-item>
              <el-form-item label="目的地区位">
                <el-select v-model="shipModel.zone" placeholder="选择">
                  <el-option label="近邻/亚洲" value="asia" />
                  <el-option label="欧洲" value="europe" />
                  <el-option label="美洲" value="america" />
                  <el-option label="非洲/大洋洲" value="others" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务类型">
                <el-select v-model="shipModel.service">
                  <el-option label="经济（海运/最慢）" value="economy" />
                  <el-option label="标准（空运/普通）" value="standard" />
                  <el-option label="快速（空运/优先）" value="express" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="estimateShipping">估算运费</el-button>
                <el-button @click="shipReset">重置</el-button>
              </el-form-item>
            </el-form>

            <div v-if="shipResult !== null" style="margin-top:12px">
              <el-alert :title="shipResult" type="info" show-icon />
            </div>
            <p style="margin-top:8px;color:#999">注：此为快速估算示例，实际运费请联系承运公司或用运费 API 校准。</p>
          </div>

          <!-- 外贸：邮件模板生成器 -->
          <div v-if="activeTool === '外贸:邮件'">
            <el-form :model="mailModel" label-width="120px">
              <el-form-item label="收件人姓名">
                <el-input v-model="mailModel.name" />
              </el-form-item>
              <el-form-item label="产品/主题">
                <el-input v-model="mailModel.subject" />
              </el-form-item>
              <el-form-item label="语气">
                <el-select v-model="mailModel.tone">
                  <el-option label="正式" value="formal" />
                  <el-option label="亲切" value="friendly" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="generateMail">生成模板</el-button>
                <el-button @click="mailReset">重置</el-button>
              </el-form-item>
            </el-form>

            <div v-if="mailOutput" style="margin-top:12px">
              <el-card>
                <pre style="white-space:pre-wrap">{{ mailOutput }}</pre>
                <el-button type="success" icon="el-icon-download" @click="downloadText(mailOutput, 'email-template.txt')">下载</el-button>
              </el-card>
            </div>
          </div>

          <!-- 程序员：JSON 格式化 -->
          <div v-if="activeTool === '程序员:json'">
            <el-row>
              <el-col :span="12">
                <el-input type="textarea" :rows="12" placeholder="在此粘贴原始 JSON" v-model="jsonRaw" />
              </el-col>
              <el-col :span="12">
                <el-input type="textarea" :rows="12" placeholder="格式化输出" :value="jsonFormatted" readonly />
              </el-col>
            </el-row>
            <div style="margin-top:8px">
              <el-button type="primary" @click="formatJson">格式化</el-button>
              <el-button @click="minifyJson">压缩</el-button>
              <el-button @click="copyToClipboard(jsonFormatted)">复制结果</el-button>
            </div>
          </div>

          <!-- 程序员：Base64 -->
          <div v-if="activeTool === '程序员:base64'">
            <el-form :model="b64" label-width="100px">
              <el-form-item label="模式">
                <el-radio-group v-model="b64.mode">
                  <el-radio label="encode">编码</el-radio>
                  <el-radio label="decode">解码</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="输入">
                <el-input type="textarea" :rows="8" v-model="b64.input" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="runBase64">执行</el-button>
                <el-button @click="b64Reset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="b64.output !== null">
              <el-card>
                <pre style="white-space:pre-wrap">{{ b64.output }}</pre>
                <el-button @click="copyToClipboard(b64.output)">复制</el-button>
              </el-card>
            </div>
          </div>

          <!-- 程序员：Cron 生成器 -->
          <div v-if="activeTool === '程序员:cron'">
            <el-form :model="cron" label-width="120px">
              <el-form-item label="分钟">
                <el-input v-model="cron.minute" placeholder="* 或 0-59 或 */5" />
              </el-form-item>
              <el-form-item label="小时">
                <el-input v-model="cron.hour" placeholder="* 或 0-23" />
              </el-form-item>
              <el-form-item label="日期">
                <el-input v-model="cron.day" placeholder="* 或 1-31" />
              </el-form-item>
              <el-form-item label="月">
                <el-input v-model="cron.month" placeholder="* 或 1-12" />
              </el-form-item>
              <el-form-item label="星期">
                <el-input v-model="cron.weekday" placeholder="* 或 0-6 / SUN-SAT" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="makeCron">生成</el-button>
                <el-button @click="cronReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="cron.result">
              <el-alert :title="`Cron: ${cron.result}`" type="success" show-icon />
            </div>
          </div>

          <!-- 学生：公式编辑器（简单 LaTeX 显示） -->
          <div v-if="activeTool === '学生:公式'">
            <el-form label-width="120px">
              <el-form-item label="LaTeX 输入">
                <el-input type="textarea" :rows="6" v-model="latexInput" placeholder="例如：\\frac{a}{b} + c^2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="renderLatex">渲染（仅展示原文）</el-button>
              </el-form-item>
            </el-form>
            <div style="margin-top:8px">
              <el-card>
                <p>注意：此处仅作为文本/占位渲染示例，若需要真正的公式渲染（MathJax/KaTeX），请在项目中引入对应库。</p>
                <pre style="white-space:pre-wrap">{{ latexInput }}</pre>
              </el-card>
            </div>
          </div>

          <!-- 学生：在线计算器（统计、矩阵基础） -->
          <div v-if="activeTool === '学生:计算器'">
            <el-form :model="calc" label-width="120px">
              <el-form-item label="数值列表（逗号分隔）">
                <el-input v-model="calc.list" placeholder="1,2,3,4" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="calcStats">计算均值/中位数/标准差</el-button>
                <el-button @click="calcReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="calc.result">
              <el-card>
                <p>均值：{{ calc.result.mean }}</p>
                <p>中位数：{{ calc.result.median }}</p>
                <p>标准差：{{ calc.result.std }}</p>
              </el-card>
            </div>
          </div>

          <!-- 学生：四六级作文批改（简单关键词与长度建议） -->
          <div v-if="activeTool === '学生:作文'">
            <el-form :model="essay" label-width="120px">
              <el-form-item label="作文原文">
                <el-input type="textarea" :rows="10" v-model="essay.text" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="gradeEssay">批改</el-button>
                <el-button @click="essayReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="essay.feedback">
              <el-card>
                <p>长度：{{ essay.feedback.length }} 字</p>
                <p>建议：{{ essay.feedback.advice }}</p>
                <p>关键词覆盖：{{ essay.feedback.keywordCount }}</p>
              </el-card>
            </div>
          </div>

          <!-- 职场：简历生成器 -->
          <div v-if="activeTool === '职场:简历'">
            <el-form :model="resume" label-width="120px">
              <el-form-item label="姓名">
                <el-input v-model="resume.name" />
              </el-form-item>
              <el-form-item label="标题/职位">
                <el-input v-model="resume.title" />
              </el-form-item>
              <el-form-item label="技能（逗号分隔）">
                <el-input v-model="resume.skills" />
              </el-form-item>
              <el-form-item label="经验简介">
                <el-input type="textarea" :rows="4" v-model="resume.summary" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="generateResume">生成简历</el-button>
                <el-button @click="resumeReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="resume.output">
              <el-card>
                <pre style="white-space:pre-wrap">{{ resume.output }}</pre>
                <el-button type="success" @click="downloadText(resume.output, 'resume.txt')">下载简历（TXT）</el-button>
              </el-card>
            </div>
          </div>

          <!-- 职场：PPT 模板下载 -->
          <div v-if="activeTool === '职场:ppt'">
            <p>已内置简洁模板占位（示例）。你可以替换为自己的 PPT 文件并放到静态资源目录（/static/templates/）。</p>
            <el-card>
              <div style="display:flex;gap:12px;flex-wrap:wrap">
                <div class="ppt-item" v-for="t in pptTemplates" :key="t.id">
                  <div class="ppt-preview">{{ t.name }}</div>
                  <el-button size="mini" @click="downloadText(t.placeholder, t.name + '.txt')">下载示例说明</el-button>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 职场：OKR 生成器 -->
          <div v-if="activeTool === '职场:okr'">
            <el-form :model="okr" label-width="120px">
              <el-form-item label="目标 Objective">
                <el-input v-model="okr.objective" />
              </el-form-item>
              <el-form-item label="关键结果（分行输入，每行一个）">
                <el-input type="textarea" :rows="6" v-model="okr.keyResults" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="generateOKR">生成 OKR</el-button>
                <el-button @click="okrReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="okr.output">
              <el-card>
                <pre style="white-space:pre-wrap">{{ okr.output }}</pre>
                <el-button @click="downloadText(okr.output, 'OKR.txt')">下载</el-button>
              </el-card>
            </div>
          </div>

        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed } from 'vue';

export default {
  name: 'ToolboxPage',
  setup() {
    const activeTool = ref('外贸:汇率');
    const activeToolTitle = computed(() => {
      const map = {
        '外贸:汇率': '汇率换算',
        '外贸:运费': '国际快递运费估算',
        '外贸:邮件': '邮件模板生成器',
        '程序员:json': 'JSON 格式化',
        '程序员:base64': 'Base64 编解码',
        '程序员:cron': 'Cron 表达式生成器',
        '学生:公式': '公式编辑器',
        '学生:计算器': '在线计算器',
        '学生:作文': '四六级作文批改',
        '职场:简历': '简历生成器',
        '职场:ppt': 'PPT 模板下载',
        '职场:okr': 'OKR/KPI 生成器',
      };
      return map[activeTool.value] || '工具箱';
    });

    function onCategorySelect(key) {
      activeTool.value = key;
    }

    // 通用
    const currencies = ['USD', 'EUR', 'CNY', 'JPY', 'GBP', 'AUD'];

    // 汇率模型
    const rateModel = ref({ from: 'USD', to: 'CNY', rate: 7.0, amount: 100 });
    const rateResult = ref(null);
    function convertRate() {
      const res = (rateModel.value.amount * Number(rateModel.value.rate)).toFixed(4);
      rateResult.value = res;
    }
    function rateReset() {
      rateModel.value = { from: 'USD', to: 'CNY', rate: 7.0, amount: 100 };
      rateResult.value = null;
    }

    // 运费
    const shipModel = ref({ weight: 1, zone: 'asia', service: 'standard' });
    const shipResult = ref(null);
    function estimateShipping() {
      const w = Number(shipModel.value.weight) || 0;
      const base = { asia: 5, europe: 10, america: 12, others: 15 };
      const svc = { economy: 0.6, standard: 1, express: 1.6 };
      const cost = (base[shipModel.value.zone] + w * 2) * svc[shipModel.value.service];
      shipResult.value = `估算运费大约 ${cost.toFixed(2)} USD（仅供参考）`;
    }
    function shipReset() {
      shipModel.value = { weight: 1, zone: 'asia', service: 'standard' };
      shipResult.value = null;
    }

    // 邮件
    const mailModel = ref({ name: '', subject: '', tone: 'formal' });
    const mailOutput = ref('');
    function generateMail() {
      const name = mailModel.value.name || 'Customer';
      const subj = mailModel.value.subject || '产品信息';
      if (mailModel.value.tone === 'formal') {
        mailOutput.value = `Dear ${name},\n\nI hope this message finds you well. I would like to introduce our product: ${subj}. Please find the details attached. If you have any questions, feel free to contact me.\n\nBest regards,\n[Your Name]`;
      } else {
        mailOutput.value = `Hi ${name},\n\nI hope you're doing great! I wanted to share something about ${subj} that might interest you. Let me know if you'd like more info.\n\nCheers,\n[Your Name]`;
      }
    }
    function mailReset() {
      mailModel.value = { name: '', subject: '', tone: 'formal' };
      mailOutput.value = '';
    }

    // JSON 工具
    const jsonRaw = ref('');
    const jsonFormatted = ref('');
    function formatJson() {
      try {
        const o = JSON.parse(jsonRaw.value);
        jsonFormatted.value = JSON.stringify(o, null, 2);
      } catch (e) {
        jsonFormatted.value = '错误：无效 JSON — ' + e.message;
      }
    }
    function minifyJson() {
      try {
        const o = JSON.parse(jsonRaw.value);
        jsonFormatted.value = JSON.stringify(o);
      } catch (e) {
        jsonFormatted.value = '错误：无效 JSON — ' + e.message;
      }
    }

    // Base64
    const b64 = ref({ mode: 'encode', input: '', output: null });
    function runBase64() {
      try {
        if (b64.value.mode === 'encode') {
          b64.value.output = btoa(unescape(encodeURIComponent(b64.value.input)));
        } else {
          b64.value.output = decodeURIComponent(escape(atob(b64.value.input)));
        }
      } catch (e) {
        b64.value.output = '错误：' + e.message;
      }
    }
    function b64Reset() {
      b64.value = { mode: 'encode', input: '', output: null };
    }

    // Cron
    const cron = ref({ minute: '*', hour: '*', day: '*', month: '*', weekday: '*', result: '' });
    function makeCron() {
      cron.value.result = `${cron.value.minute} ${cron.value.hour} ${cron.value.day} ${cron.value.month} ${cron.value.weekday}`;
    }
    function cronReset() {
      cron.value = { minute: '*', hour: '*', day: '*', month: '*', weekday: '*', result: '' };
    }

    // LaTeX placeholder
    const latexInput = ref('');
    function renderLatex() {
      // placeholder — real render requires MathJax/KaTeX
    }

    // 计算器统计
    const calc = ref({ list: '', result: null });
    function calcStats() {
      const arr = calc.value.list
        .split(',')
        .map(s => parseFloat(s.trim()))
        .filter(n => !Number.isNaN(n));
      if (!arr.length) {
        calc.value.result = null; return;
      }
      const mean = arr.reduce((a,b) => a+b,0) / arr.length;
      const sorted = arr.slice().sort((a,b)=>a-b);
      const mid = sorted.length%2===1 ? sorted[(sorted.length-1)/2] : (sorted[sorted.length/2-1]+sorted[sorted.length/2])/2;
      const std = Math.sqrt(arr.reduce((a,b)=>a+(b-mean)*(b-mean),0)/arr.length);
      calc.value.result = { mean: mean.toFixed(4), median: mid, std: std.toFixed(4) };
    }
    function calcReset(){ calc.value = { list: '', result: null }; }

    // 作文批改（简单规则）
    const essay = ref({ text: '', feedback: null });
    const keywords = ['government','environment','technology','economy','education'];
    function gradeEssay() {
      const txt = essay.value.text || '';
      const len = txt.replace(/\s+/g,'').length;
      const count = keywords.reduce((c,kw)=> c + (new RegExp(kw,'i').test(txt) ? 1 : 0), 0);
      let advice = '长度合适，注意语句连贯。';
      if (len < 200) advice = '字数偏少，建议增加论据与细节。';
      else if (len > 800) advice = '字数偏多，建议精简语句，注意结构。';
      essay.value.feedback = { length: len, advice, keywordCount: count };
    }
    function essayReset(){ essay.value = { text: '', feedback: null }; }

    // 简历生成
    const resume = ref({ name:'', title:'', skills:'', summary:'', output: '' });
    function generateResume(){
      const s = resume.value;
      resume.value.output = `姓名：${s.name}\n职位：${s.title}\n技能：${s.skills}\n简介：${s.summary}\n\n（将此文本复制到 Word 或在线简历模板中继续美化）`;
    }
    function resumeReset(){ resume.value = { name:'', title:'', skills:'', summary:'', output: '' }; }

    // PPT 模板占位
    const pptTemplates = [{ id:1, name:'简洁商务模板', placeholder:'这是一个简洁商务 PPT 模板示例说明。' }, { id:2, name:'创意报告模板', placeholder:'创意报告 PPT 模板示例说明。' }];

    // OKR
    const okr = ref({ objective:'', keyResults:'', output:'' });
    function generateOKR(){
      const lines = okr.value.keyResults.split('\n').map(l=>l.trim()).filter(Boolean);
      okr.value.output = `Objective: ${okr.value.objective}\n` + lines.map((l,i)=>`KR${i+1}: ${l}`).join('\n');
    }
    function okrReset(){ okr.value = { objective:'', keyResults:'', output:'' }; }

    // 下载文本
    function downloadText(text, filename) {
      const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = filename || 'file.txt';
      a.click(); URL.revokeObjectURL(url);
    }

    function copyToClipboard(text) {
      navigator.clipboard?.writeText(text).then(()=>{
        alert('已复制到剪贴板');
      }).catch(()=>{ alert('复制失败，请手动复制：\n'+text); });
    }

    return {
      activeTool,
      activeToolTitle,
      onCategorySelect,
      currencies,
      rateModel, rateResult, convertRate, rateReset,
      shipModel, shipResult, estimateShipping, shipReset,
      mailModel, mailOutput, generateMail, mailReset,
      jsonRaw, jsonFormatted, formatJson, minifyJson,
      b64, runBase64, b64Reset,
      cron, makeCron, cronReset,
      latexInput, renderLatex,
      calc, calcStats, calcReset,
      essay, gradeEssay, essayReset,
      resume, generateResume, resumeReset,
      pptTemplates,
      okr, generateOKR, okrReset,
      downloadText, copyToClipboard
    };
  }
};
</script>

<style scoped>
.toolbox-page { padding: 18px; }
.category-menu { width: 100%; }
.ppt-item { border:1px dashed #ddd; padding:12px; border-radius:6px; width:180px; text-align:center }
.ppt-preview { height:100px; display:flex; align-items:center; justify-content:center; background:#fafafa; margin-bottom:8px }
</style>
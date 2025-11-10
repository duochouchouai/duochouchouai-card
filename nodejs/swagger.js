// swagger.js
const yaml = require('yamljs');
const path = require('path');

// 动态加载YAML文档文件（支持热更新）
const loadOpenAPIDoc = () => {
    try {
        // 每次调用都重新读取文件，支持热更新
        console.log('重新加载OpenAPI文档...');
        
        // 加载主文档
        const mainDoc = yaml.load(path.join(__dirname, 'docs','index.yaml'));
        // 加载业务相关路径
        const codeDoc = yaml.load(path.join(__dirname, 'docs','code.yaml'));
        // 加载认证相关路径
        const authDoc = yaml.load(path.join(__dirname, 'docs','auth.yaml'));

        // 加载共享组件
        const componentsDoc = yaml.load(path.join(__dirname, 'docs','components.yaml'));
        
        // 合并文档
        const mergedDoc = {
            ...mainDoc,
            paths: {
                ...(mainDoc.paths || {}),
                ...(authDoc.paths || {}),
                ...(codeDoc.paths || {})
            },
            components: {
                ...(mainDoc.components || {}),
                ...(componentsDoc.components || {})
            }
        };
        
        return mergedDoc;
    } catch (error) {
        console.error('加载OpenAPI文档失败:', error);
        throw error;
    }
};

// 导出加载函数而非静态结果，支持热更新
module.exports = {
    loadOpenAPIDoc,
    // 提供get方法供直接访问，向后兼容
    get swaggerDocs() {
        return loadOpenAPIDoc();
    }
};
export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.stack = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static matMul(param_0, param_1, param_2, param_3, param_4): void     {
        let v10 = v3;
        v10 = v27[v12];
        length = v10.length;
        v10 = v27;
        length = v10.length;
        v10 = [];
        v11 = 0;
        v12 = v11;
        let v12: Array<unknown> = [];
        const v14 = 0;
        const v15: number = v14;
        const v15 = 0;
        const v17 = 0;
        const v17 = 0;
        let v21 = v3;
        let v22 = v11;
        let v20 = v21[v22];
        v21 = v17;
        const v19 = v20[v21];
        v21 = v27;
        v22 = v17;
        v20 = v21[v22];
        v21 = v14;
        const v15: number = v18 + v19 * v20[v21];
        v18 = v17;
        const number: number = Number(v18);
        const v17: number = _number + 1;
        for (let v15 = 0; !(v18 < length3); v18) {
            let v18 = v17;
        }
        const v17 = _number;
        const _number = v15;
        v17.push(_number);
        const v15 = v14;
        const number: number = Number(v15);
        const v14: number = _number2 + 1;
    }

    static add(param_0, param_1, param_2): void     {
        let v9 = v2;
        let stack = v9.stack;
        let pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        v9 = v12;
        stack = v9.stack;
        pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        pop = pop3;
        const v5: number = pop + pop2;
        stack = v12.stack;
        stack.push(v5);
        return v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static testRPN(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v7: acc = acc;
        v7.push(5);
        v7 = acc;
        push = v7.push;
        v8 = 3;
        v7.push(v8);
        v7 = acc;
        v7.add();
        v7 = acc;
        push = v7.push;
        v8 = 2;
        v7.push(v8);
        v7 = acc;
        v7.multiply();
        v7 = acc;
        let result = v7.result;
        v7.result();
        result = v7.result();
        result = String;
        v7 = result2;
        return result(v7);
    }

    static push(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const stack = v7.stack;
        stack.push(v11);
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v17;
        lex_0_1 = func_main_0;
        v9 = anonymous_method;
        lex_0_0 = v9;
        v9 = func_main_0;
        lex_0_5 = v9;
        v9 = onCreate;
        lex_0_3 = v9;
        v9 = onWindowStageDestroy;
        lex_0_2 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", anonymous_method);
        prototype = undefined;
        let v0: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v11 = set * undefined;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = caesarDecrypt;
        false[v15] = "get".v16;
        prototype2.initialRender = caesarEncrypt;
        prototype2.rerender = testFibIterative;
        set.getEntryName = fibonacciIterative;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = purgeVariableDependenciesOnElmtId;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static result(param_0, param_1, param_2): void     {
        let v5 = v2;
        let stack = v5.stack;
        stack = v11.stack;
        v5 = stack2.length - 1;
        return stack[v5];
    }

    static testCaesar(param_0, param_1, param_2): void     {
        let v6 = lex_0_0;
        v6 = v6("Hello World", 3);
        v6 = lex_0_1;
        v8 = 3;
        v6 = v6(v6, v8);
        v6 = `${v6}|`;
        return v6 + v62;
    }

    static testMatMul(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        let v8: string[] = ["/* element_0 */"];
        ["/* element_0 */"][7] = v17;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][7] = v18;
        const v3: Array<unknown> = v7;
        v7 = [];
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */"][7] = v17;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */"][7] = v18;
        const v4: Array<unknown> = v7;
        v7 = lex_0_3;
        v8 = v3;
        v7 = v7(v8, v4);
        let v13 = String;
        let v15: Array<unknown> = v7;
        const v16 = 0;
        let v14 = v15[v16];
        v15 = 0;
        v14 = v14[v15];
        v13 = v13(v14);
        let v11: string = `${v13},`;
        v13 = String;
        v14 = v7;
        v15 = 0;
        v13 = v14[v15];
        v14 = 1;
        v13 = v13[v14];
        v9 = `${v11 + v13(v13)},`;
        v10 = String;
        v13 = v7;
        v13 = 1;
        v11 = v13[v13];
        v13 = 0;
        v11 = v11[v13];
        v8 = v9 + v10(v11);
        v7 = `${v8},`;
        v8 = String;
        v10 = v7;
        v11 = 1;
        v9 = v10[v11];
        v10 = 1;
        v9 = v9[v10];
        return v7 + v8(v9);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static multiply(param_0, param_1, param_2): void     {
        let v9 = v2;
        let stack = v9.stack;
        let pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        v9 = v12;
        stack = v9.stack;
        pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        pop = pop3;
        const v5: number = pop * pop2;
        stack = v12.stack;
        stack.push(v5);
        return v5;
    }

    static subtract(param_0, param_1, param_2): void     {
        let v9 = v2;
        let stack = v9.stack;
        let pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        v9 = v12;
        stack = v9.stack;
        pop = stack.pop;
        stack.pop();
        pop = stack.pop();
        pop = pop3;
        const v5: number = pop - pop2;
        stack = v12.stack;
        stack.push(v5);
        return v5;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testWeighted(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v8 = ["/* element_0 */"];
        const v6: string[] = ["/* element_0 */"];
        v8 = lex_0_2;
        v8 = v8(v3, v6, 0);
        v8 = lex_0_2;
        v9 = v3;
        v11 = 4;
        v8 = v8(v9, v6, v11);
        v8 = `${v8},`;
        return v8 + v82;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static caesarDecrypt(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = lex_0_0;
        const v7 = v3;
        let v8 = v4;
        v8 = -v8;
        return v6(v7, v8);
    }

    static caesarEncrypt(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = 0;
        let v12 = v3;
        v12.charAt(v8);
        charAt = v12.charAt(v13);
        v12 = charAt2;
        let charCodeAt = v12.charCodeAt;
        v13 = 0;
        v12.charCodeAt(v13);
        charCodeAt = v12.charCodeAt(v13);
        charCodeAt = charCodeAt2;
        charCodeAt = charCodeAt2;
        v13 = (charCodeAt2 - 65 + v21) % 26;
        v12 = v13 + 26;
        charCodeAt = v12 % 26;
        const charCodeAt2: number = charCodeAt + 65;
        charCodeAt += 65;
        let v13 = String;
        v13.fromCharCode(charCodeAt2);
        const charAt2 = v8;
        const number: number = Number(charAt2);
        const v8: number = _number + 1;
    }

    static weightedSelect(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v3 = v18;
        v4 = v19;
        v5 = v20;
        const v8 = 0;
        const v11 = 0;
        v3 = v18;
        v4 = v19;
        v5 = v20;
        const v8 = 0;
        const v11 = 0;
        const v13 = v4;
        const v14 = v11;
        const v8: number = v12 + v13[v14];
        v12 = v11;
        const number: number = Number(v12);
        const v11: number = _number + 1;
        for (v0 = v15; !(v12 < v13.length); v12) {
            let v12 = v11;
            const v13 = v4;
        }
        const v10 = _number;
        const v7: number = v10 % v8;
        const v6 = 0;
        const v11 = 0;
        let _number = v11;
        const v13 = v3;
        while (!(_number < v13.length)) {
            let _number = v6;
            const v13 = v4;
            const v14 = v11;
            const v6: number = _number + v13[v14];
            _number = v7;
            if (!(_number < v6)) {
                let _number: boolean = _number < v6;
                const number: number = Number(_number);
                const v11: number = _number + 1;
                continue;
            }
            let _number = v3;
            const v13 = v11;
            return _number[v13];
        }
        const v13 = v3;
        const length = v13.length;
        const v11: number = length - 1;
        return v10[v11];
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testFibIterative(param_0, param_1, param_2): void     {
        let v7 = lex_0_5;
        v7 = v7(10);
        v7 = lex_0_5;
        v8 = 15;
        v7 = v7(v8);
        v7 = lex_0_5;
        v8 = 20;
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static fibonacciIterative(param_0, param_1, param_2, param_3): void     {
        return v17;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}
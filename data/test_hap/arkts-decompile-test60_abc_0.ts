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
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
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
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
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

    constructor(param_0, param_1, param_2, param_3)     {
        let v6: Array<unknown> = [];
        v5.buffer = [];
        v5 = v9;
        v5.size = 0;
        v5 = v9;
        v5.head = 0;
        v5 = v9;
        v5.count = 0;
        v5 = v9;
        v5.size = v10;
        v5 = v9;
        v6 = [];
        v5.buffer = [];
        v5 = v9;
        v5.head = 0;
        v5 = v9;
        v5.count = 0;
        return v9;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static topK(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v3;
        v9.slice(0);
        slice = v9.slice(v10);
        v9 = 0;
        v10 = v9;
        let v10 = v9;
        const v14 = v9;
        let v13: number = v14 + 1;
        const v14 = v13;
        let v15 = slice2;
        while (!(v14 < v15.length)) {
            let v15 = slice2;
            let v16 = v13;
            const v14 = v15[v16];
            v15 = slice2;
            v16 = v10;
            if (v14 > v15[v16]) {
                let v10 = v13;
                const v14 = v13;
                const number: number = Number(v14);
                let v13: number = _number + 1;
                continue;
            }
        }
        let v12: boolean = v14 > v15[v16];
        let v13 = v9;
        const v11 = v12[v13];
        v12 = slice2;
        v13 = v9;
        const _number = slice2;
        let v15 = v10;
        v12[v13] = _number[v15];
        v12 = slice2;
        v13 = v10;
        v12[v13] = v11;
        let v10 = v9;
        const number: number = Number(v10);
        let v9: number = _number2 + 1;
        const _number2 = _number2;
        while (!(_number2 < v4)) {
            loop_0: do {
    let _number2: boolean = _number2 < v4;
} while (_number2 < v4);
        }
        return _number2 < v21;
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

    static testRLE(param_0, param_1, param_2): void     {
        let v6 = lex_0_3;
        v6 = v6("AAABBBCCDAA");
        v6 = lex_0_3;
        v7 = "XYZ";
        v6 = v6(v7);
        v6 = `${v6}|`;
        return v6 + v62;
    }

    static push(param_0, param_1, param_2, param_3): void     {
        let v6 = v2;
        const count = v6.count;
        if (!(count < v6.size)) {
            const hasSize: boolean = count < v6.size;
            let buffer = hasSize.buffer;
            const v7 = v2;
            let head = v7.head;
            buffer[head] = v3;
            buffer = v2;
            const v8 = v2;
            head = v8.head;
            head = head2 + 1;
            let head2 = v2;
            buffer.head = head % head2.size;
            return;
        }
        let head2 = v2;
        let buffer = head2.buffer;
        head2 = v12;
        buffer2.push(head2);
        const buffer2 = v2;
        const count = buffer2.count;
        const number: number = Number(count);
        buffer2.count = _number + 1;
    }

    static testTopK(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_2;
        v6 = v6(v3, 3);
        v8 = ",";
        v6.join(v8);
        return v6.join(v8);
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
        lex_0_4 = func_main_0;
        v9 = anonymous_method;
        lex_0_3 = v9;
        v9 = onForeground;
        lex_0_2 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", testTopK);
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = testBracketMatch;
        false[v15] = "get".v16;
        prototype2.initialRender = initialRender;
        prototype2.rerender = updateStateVars;
        set.getEntryName = purgeVariableDependenciesOnElmtId;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_45;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static isBalanced(param_0, param_1, param_2, param_3): void     {
        const v4: Array<unknown> = [];
        const v7 = 0;
        let v10 = v3;
        v10.charAt(v7);
        charAt = v10.charAt(v11);
        v10 = charAt2;
        charAt = v10 === "(";
        v10 = charAt2 % acc;
        let charAt = charAt2;
        let v10 = v4;
        const v11 = charAt2;
        v10.push(v11);
        return false;
    }

    static toArray(param_0, param_1, param_2): void     {
        let v6 = v2;
        const count = v6.count;
        if (!(count < v6.size)) {
            let hasSize: boolean = count < v6.size;
            hasSize = count < v6.size;
            let v6 = 0;
            const v7 = v6;
            const v8 = v2;
            while (!(v7 < v8.size)) {
                loop_0: do {
    let hasSize: boolean = v7 < v8.size;
    let v8 = v2;
} while (hasSize3 < v8.size);
            }
            return hasSize3 < v8.size;
        }
        let hasSize3 = v2;
        const buffer = hasSize3.buffer;
        hasSize3 = 0;
        buffer.slice(hasSize3);
        return buffer.slice(hasSize3);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static tryAcquire(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        v7 = v7 - v12.lastRefill;
        if (v7 > 0) {
            let v9 = v2;
            let tokens = v9.tokens;
            let v7: number = tokens + v5;
            tokens = v7;
            v9 = v2;
            if (tokens > v9.maxTokens) {
                let tokens = v2;
                const maxTokens = tokens.maxTokens;
                let tokens = v2;
                tokens.tokens = maxTokens;
                tokens = v2;
                tokens.lastRefill = v3;
            }
            const hasMaxTokens: boolean = tokens > v9.maxTokens;
            let tokens = hasMaxTokens.tokens;
        }
        tokens = v12.tokens;
        const number: number = Number(tokens);
        hasMaxTokens.tokens = _number - 1;
        return true;
    }

    static runLengthEncode(param_0, param_1, param_2, param_3): void     {
        const v9 = v3;
        return "";
    }

    static testRateLimiter(param_0, param_1, param_2): void     {
        let v11 = 3;
        let v12 = 0;
        v11 = new 0();
        let tryAcquire = v11.tryAcquire;
        v12 = 0;
        v11.tryAcquire(v12);
        tryAcquire = v11.tryAcquire(v12);
        v11 = v3;
        tryAcquire = v11.tryAcquire;
        v12 = 0;
        v11.tryAcquire(v12);
        tryAcquire = v11.tryAcquire(v12);
        v11 = v3;
        tryAcquire = v11.tryAcquire;
        v12 = 0;
        v11.tryAcquire(v12);
        tryAcquire = v11.tryAcquire(v12);
        v11 = v3;
        tryAcquire = v11.tryAcquire;
        v12 = 0;
        v11.tryAcquire(v12);
        tryAcquire = v11.tryAcquire(v12);
        v11 = v3;
        tryAcquire = v11.tryAcquire;
        v12 = 5;
        v11.tryAcquire(v12);
        tryAcquire = v11.tryAcquire(v12);
        let v18 = String;
        v18 = v18(tryAcquire2);
        let v16: string = `${v18},`;
        v18 = String;
        v18 = tryAcquire3;
        let v15: number = v16 + v18(v18);
        let v14: string = `${v15},`;
        v15 = String;
        v16 = tryAcquire4;
        v12 = `${v14 + v15(v16)},`;
        v13 = String;
        v14 = tryAcquire5;
        v11 = v12 + v13(v14);
        tryAcquire = `${v11},`;
        v11 = String;
        v12 = tryAcquire6;
        return tryAcquire + v11(v12);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testBracketMatch(param_0, param_1, param_2): void     {
        let v8 = lex_0_4;
        v8 = v8("()[]{}");
        v8 = lex_0_4;
        v9 = "([)]";
        v8 = v8(v9);
        v8 = lex_0_4;
        v9 = "{[]}";
        v8 = v8(v9);
        v8 = lex_0_4;
        v9 = "(";
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `${v14},`;
        v14 = String;
        let v11: number = v12 + v14(v14);
        let v10: string = `${v11},`;
        v11 = String;
        v9 = v10 + v11(v83);
        v8 = `${v9},`;
        v9 = String;
        v10 = v84;
        return v8 + v9(v10);
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

    static testCircularBuffer(param_0, param_1, param_2): void     {
        let v7 = 3;
        v7 = new 3();
        v7.push(1);
        v7 = v3;
        push = v7.push;
        v8 = 2;
        v7.push(v8);
        v7 = v3;
        push = v7.push;
        v8 = 3;
        v7.push(v8);
        v7 = v3;
        push = v7.push;
        v8 = 4;
        v7.push(v8);
        v7 = v3;
        push = v7.push;
        v8 = 5;
        v7.push(v8);
        v7 = v3;
        v7.toArray();
        toArray = v7.toArray();
        v7 = toArray2;
        v8 = ",";
        v7.join(v8);
        return v7.join(v8);
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
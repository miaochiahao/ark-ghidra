export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.items = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static pop(param_0, param_1, param_2): void     {
        const v7 = v2;
        let items = v7.items;
        items = items.length;
        if (!(items === 0)) {
            items = v7.items;
            let v8 = length;
            const v7: number = 1 - v8;
            const v3 = items[v7];
            v8 = v2;
            items = v8.items;
            const splice = items.splice;
            v8 = length;
            v8 = 1 - v8;
            const v9 = 1;
            return v3;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static peek(param_0, param_1, param_2): void     {
        const v6 = v2;
        let items = v6.items;
        items = items.length;
        if (!(items === 0)) {
            items = v6.items;
            const v7 = length;
            const v6: number = 1 - v7;
            return items[v6];
        }
    }

    static push(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const items = v7.items;
        return;
    }

    static size(param_0, param_1, param_2): void     {
        const v5 = v2;
        const items = v5.items;
        return items.length;
    }

    static adder(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return acc + v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_1 = func_0;
        v13 = func_1;
        lex_0_0 = v13;
    }

    static makeAdder(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v10;
        return func_0;
    }

    static testStack(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        let v10 = 10;
        v9 = acc;
        push = v9.push;
        v10 = 20;
        v9 = acc;
        push = v9.push;
        v10 = 30;
        v9 = acc;
        let peek = v9.peek;
        peek = v9.peek();
        v9 = acc;
        let pop = v9.pop;
        pop = v9.pop();
        v9 = acc;
        let size = v9.size;
        size = v9.size();
        let v12 = String;
        v12 = v12(peek);
        v10 = `,${v12}`;
        v12 = String;
        v12 = pop;
        v9 = v12(v12) + v10;
        size = `,${v9}`;
        v9 = String;
        v10 = size;
        return v9(v10) + size;
    }

    static double(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return 2 * v5;
    }

    static applyTwice(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = v4;
        v8 = v8(v13);
        v8 = v14;
        v9 = v8;
        v8 = v8(v9);
        return v8;
    }

    static getCode(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.code;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testArrayFind(param_0, param_1, param_2): void     {
        const v7: Array<unknown> = [];
        const v5: Array<unknown> = [];
        const v3 = undefined;
        const v4 = 0;
        let v8 = v5;
        if (v8.length < v7) {
            let v9 = v5;
            const v10 = v4;
            let v8 = v9[v10];
            let startsWith = v8.startsWith;
            v9 = "ch";
            startsWith = v5;
            v8 = v4;
            const v3 = startsWith[v8];
        }
        return v3;
    }

    static testJsonParse(param_0, param_1, param_2): void     {
        const v3 = "{\"name\":\"test\",\"value\":42}";
        let v9 = JSON;
        let parse = v9.parse;
        parse = v9.parse(v3);
        parse = parse;
        v9 = "name";
        const v4 = parse[v9];
        parse = parse;
        v9 = "value";
        const v6 = parse[v9];
        v9 = v4;
        parse = `=${v9}`;
        v9 = String;
        return v9(v6) + parse;
    }

    static getMessage(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.msg;
    }

    static testCustomError(param_0, param_1, param_2): void     {
        let v7 = "not found";
        const v3 = new "not found"();
        v7 = String;
        const v9 = v3;
        let code = v9.getCode;
        code = v9.getCode();
        v7 = v7(code);
        const v5: string = `:${v7}`;
        return v3.getMessage() + v5;
    }

    static testHigherOrder(param_0, param_1, param_2): void     {
        const v3 = func_0;
        let v6 = lex_0_1;
        v6 = v6(3, v3);
        v6 = String;
        return v6(v6);
    }

    static testPropDefault(param_0, param_1, param_2): void     {
        {  }[10] = "x";
        v7 = v3;
        v8 = "x";
        const v4 = v7[v8];
        v7 = v3;
        v8 = "y";
        const v5 = v7[v8];
        let v9 = String;
        v9 = v9(v4);
        v7 = `,${v9}`;
        v9 = String;
        return v5(v5) + v7;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testMapGetDefault(param_0, param_1, param_2): void     {
        const v7 = Map;
        const map = new Map(v7);
        let v8 = map;
        let v9 = "a";
        let v10 = 1;
        v8 = map;
        set = v8.set;
        v9 = "b";
        v10 = 2;
        const v4 = "c";
        v8 = map;
        v9 = v4;
        v9 = map;
        get = v9.get(v4);
        get = get;
    }

    static testClosureFactory(param_0, param_1, param_2): void     {
        let v8 = lex_0_0;
        v8 = v8(5);
        v8 = lex_0_0;
        v9 = 10;
        v8 = v8(v9);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        let v10 = String;
        v10 = v10(v8);
        v8 = `,${v10}`;
        v10 = String;
        return v8(v8) + v8;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testStringStartsEnds(param_0, param_1, param_2): void     {
        const v4 = "Hello, World!";
        let v9: string = v4;
        let startsWith = v9.startsWith;
        startsWith = v9.startsWith("Hello");
        v9 = v4;
        let endsWith = v9.endsWith;
        v10 = "!";
        endsWith = v9.endsWith(v10);
        v9 = v4;
        startsWith = v9.startsWith;
        v10 = "World";
        startsWith = v9.startsWith(v10, 7);
        let v12 = String;
        v12 = v12(startsWith);
        v10 = `,${v12}`;
        v12 = String;
        v12 = endsWith;
        v9 = v12(v12) + v10;
        startsWith = `,${v9}`;
        v9 = String;
        v10 = startsWith;
        return v9(v10) + startsWith;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
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
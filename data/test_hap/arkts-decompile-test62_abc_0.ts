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
        {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v11 = JSON;
        const v12 = v3;
        v11.stringify(v12);
        stringify = v11.stringify(v12);
        v6.error(v7, "testTag", "Failed to load the content. Cause: %{public}s");
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        let v4 = 0;
        lex_0_0 = v4;
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
        const v11 = "Ability onCreate";
        error.info(v8, "testTag", "%{public}s");
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        v6.info(v7, v8, "%{public}s");
        const v6 = v3;
        v8 = onBackground;
        v6.loadContent("pages/Index", v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
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
        let v4 = 0;
        lex_0_0 = v4;
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

    constructor(param_0, param_1, param_2)     {
        v4.value = 0;
        v4 = v7;
        v4.active = false;
        return v7;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        let v8 = "100%";
        v7.width(v8);
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static from(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.tableName = v9;
        return v2;
    }

    static build(param_0, param_1, param_2): void     {
        const v3: number = "SELECT * FROM " + v2.tableName;
        if (length > 0) {
            let conditions = v3;
            let length: string = `${conditions} WHERE `;
            let v8 = v2;
            conditions = v8.conditions;
            const join = conditions2.join;
            v8 = " AND ";
            conditions2.join(v8);
            const v3: number = length + conditions2.join(v8);
            const conditions2 = v2;
            let orderField = conditions2.orderField;
            length = orderField.length;
            if (length > 0) {
                let orderField = v3;
                let length: string = `${orderField} ORDER BY `;
                orderField = v2;
                const v3: number = length + orderField.orderField;
            }
            return length > 0;
        }
        let orderField: boolean = v2.conditions.length > 0;
        let limitValue = orderField.limitValue;
        let orderField = v3;
        let limitValue: string = `${orderField} LIMIT `;
        orderField = String;
        limitValue = v11.limitValue;
        const v3: number = limitValue + orderField(limitValue2);
    }

    static limit(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.limitValue = v9;
        return v2;
    }

    static where(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const conditions = v7.conditions;
        conditions.push(v11);
        return v10;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        let message = lex_0_0.message;
        v7.create(message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_3 = func_main_0;
        let v13 = ViewPU;
        let v13 = Reflect;
        let set = v13.set;
        let prototype = ViewPU.prototype;
        let v16 = build;
        v13.set(prototype, "finalizeConstruction", v16);
        prototype = undefined;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        const v5 = set;
        prototype = v5;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = v5;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        set = @system.app;
        v13 = @system.app;
        prototype = set.prototype;
        v16 = "message";
        let v17 = undefined;
        false[v18] = "get".v17;
        v16 = "message";
        v17 = undefined;
        v18 = QueryBuilder;
        false[v17] = "get".v18;
        prototype2.initialRender = getCallCount;
        prototype2.rerender = initialRender;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        v13 = func_58;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static deepClone(param_0, param_1, param_2, param_3): void     {
        const v4: Array<unknown> = [];
        const v8 = 0;
        do {
            const v10 = v4;
            let push = v10.push;
            let v12 = v3;
            let tags = v12.tags;
            v12 = v8;
            tags = tags[v12];
            v10.push(tags);
            push = v8;
            const number: number = Number(push);
            const v8: number = _number + 1;
        } while (_number);
    }

    static testShapes(param_0, param_1, param_2): void     {
        let v7 = 5;
        v7 = 4;
        const v9 = new 5();
        let describe = v9.describe;
        v9.describe();
        describe = v9.describe();
        const v6: string = `${describe2}|`;
        describe = new 6(v6, v7);
        describe = describe.describe;
        describe.describe();
        return v6 + describe.describe();
    }

    static getArea(param_0, param_1, param_2): void     {
        let v6 = v2;
        const v4: number = v6.radius * v6.radius;
        return v4 * 3;
    }

    static getArea(param_0, param_1, param_2): void     {
        let v5 = v2;
        const w = v5.w;
        return w * v8.h;
    }

    static orderBy(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.orderField = v9;
        return v2;
    }

    static tryCall(param_0, param_1, param_2, param_3): void     {
        v2 = v10;
        let v6 = v3;
        v6 = v2;
        v2.lastCall = v11;
        let v6 = v2;
        const number: number = Number(v6.callCount);
        v6.callCount = _number + 1;
        return true;
    }

    static acquire(param_0, param_1, param_2): void     {
        const available = v2.available;
        const length = available.length;
        if (length <= 0) {
            const acc: acc = new acc(length, available, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23);
            const length: acc = acc;
            length.active = true;
            const available = v2;
            const created = available.created;
            const number: number = Number(created);
            available.created = _number + 1;
            let v7 = v2;
            let inUse = v7.inUse;
            const push = inUse.push;
            v7 = acc;
            inUse.push(v7);
            return acc;
        }
        let v8 = v2;
        const available = v8.available;
        available.pop();
        pop = available.pop();
        pop = pop2;
        pop.active = true;
        v8 = v11;
        inUse = v8.inUse;
        v8 = pop2;
        inUse.push(v8);
        return pop2;
    }

    static release(param_0, param_1, param_2, param_3): void     {
        v6.active = false;
        v6 = v14;
        v6.value = 0;
        v6 = 1;
        let inUse = v2.inUse;
        while (v8 >= inUse.length) {
            let v10 = v2;
            inUse = v10.inUse;
            v10 = v7;
            const v8 = inUse[v10];
            if (v8 === v3) {
                const v4 = v7;
                const v8 = v7;
                const number: number = Number(v8);
                const v7: number = _number + 1;
                continue;
            }
        }
        let _number = v2;
        let inUse = _number.inUse;
        _number = -(0 === v14);
        inUse = 1;
        inUse2.splice(_number, inUse);
        _number = v13;
        const available = _number.available;
        _number = v14;
        available.push(_number);
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const __message = v2.__message;
        __message.get();
        return __message.get();
    }

    static describe(param_0, param_1, param_2): void     {
        let v7 = String;
        const v9 = v2;
        let area = v9.getArea;
        v9.getArea();
        area = v9.getArea();
        let v5: number = "area=" + v7(area);
        const v4: string = `${v5},perimeter=`;
        v5 = String;
        let perimeter = v7.getPerimeter;
        v12.getPerimeter();
        perimeter = v12.getPerimeter();
        return v4 + v5(perimeter);
    }

    static getStats(param_0, param_1, param_2): void     {
        let v8 = String;
        let created = v2.created;
        v8 = v8(created);
        v8 = String;
        created = v12;
        let length = created.available.length;
        let v5: number = `${v8},` + v8(length);
        const v4: string = `${v5},`;
        v5 = String;
        length = v12.inUse.length;
        return v4 + v5(length2);
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

    static testDeepClone(param_0, param_1, param_2): void     {
        let v6 = {  };
        let v7: string[] = ["/* element_0 */"];
        v6.tags = ["/* element_0 */"];
        const v4 = v6;
        v6 = lex_0_3;
        v6 = v6(v4);
        let v8 = v6;
        let tags = v8.tags;
        let push = tags.push;
        v8 = "d";
        tags.push(v8);
        tags = v4.tags;
        push = `${tags2.length},`;
        tags = v6.tags;
        return push + tags.length;
    }

    static testThrottler(param_0, param_1, param_2): void     {
        let v6 = 100;
        const v3 = new 100();
        v6 = v3;
        let tryCall = v6.tryCall;
        let v7 = 50;
        v6.tryCall(v7);
        v6 = v3;
        tryCall = v6.tryCall;
        v7 = 120;
        v6.tryCall(v7);
        tryCall = v3.tryCall;
        v7 = 150;
        v3.tryCall(v7);
        tryCall = v3.tryCall;
        v7 = 250;
        v3.tryCall(v7);
        tryCall = String;
        v7 = v3;
        let callCount = v7.getCallCount;
        v7.getCallCount();
        callCount = v7.getCallCount();
        return tryCall(callCount);
    }

    static testObjectPool(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.acquire();
        acquire = v9.acquire();
        v9 = acc;
        acquire = v9.acquire;
        v9.acquire();
        acquire = v9.acquire();
        v9 = acc;
        v9.release(acquire2);
        v9 = acc;
        acquire = v9.acquire;
        v9.acquire();
        acquire = v9.acquire();
        v9 = acc;
        v9.getStats();
        return v9.getStats();
    }

    static getPerimeter(param_0, param_1, param_2): void     {
        const v4: number = 2 * v2.radius;
        return v4 * 3;
    }

    static getPerimeter(param_0, param_1, param_2): void     {
        const v4 = 2;
        let v6 = v2;
        const w = v6.w;
        return v4 * (w + v9.h);
    }

    static getCallCount(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.callCount;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testQueryBuilder(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let from = acc.from;
        const v17 = "users";
        acc.from(v17);
        from = acc.from(v17);
        let where = from2.where;
        from = "age > 18";
        from2.where(from);
        where = from2.where(from);
        where = where2.where;
        where = "active = true";
        where2.where(where);
        where = where2.where(where);
        let orderBy = where4.orderBy;
        const where3 = "name";
        where4.orderBy(where3);
        orderBy = where4.orderBy(where3);
        orderBy = 10;
        orderBy2.limit(orderBy);
        limit = orderBy2.limit(orderBy);
        limit2.build();
        build = limit2.build();
        return build2;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        let v6 = func_main_0;
        let v7 = Column;
        v5.observeComponentCreation2(v6, v7);
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

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const __message = v2.__message;
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
        v2 = v9;
        v2.message = v10.message;
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